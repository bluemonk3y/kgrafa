/**
 * Copyright 2018 Confluent Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kgrafa;

import io.confluent.kgrafa.model.metric.Metric;
import io.confluent.kgrafa.model.metric.MetricStats;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.confluent.kgrafa.model.metric.MetricStats.MetricStatsSerde;

public class MetricStatsCollector {
    private static final Logger log = LoggerFactory.getLogger(MetricStatsCollector.class);

    public static final int RETENTION = 1000;
    private final Topology topology;
    private MetricStats currentWindowStats;
    private final Properties streamsConfig;
    private long startTime;
    private long endTime;
    private KafkaStreams streams;
    private final Queue<MetricStats> stats = new ConcurrentLinkedQueue<>();
    private long windowDuration;
    private long processedLast = 0;

    public MetricStatsCollector(final String topic, final Properties streamsConfig, final long windowDuration, final long startTime, final long endTime) {
        this.streamsConfig = streamsConfig;
        this.startTime = startTime;
        this.endTime = endTime;
        this.windowDuration = windowDuration;
        this.topology = buildTopology(topic);
    }

    private Topology buildTopology(final String metricTopic) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Metric> tasks = builder.stream(metricTopic);

        log.debug("Duration:" + windowDuration + "ms " + new Date(startTime) + " - " + new Date(endTime) + " Topic: " + metricTopic + " Window:" + windowDuration);

        KTable<Windowed<String>, MetricStats> windowedTaskStatsKTable = tasks
                .filter((key, value) -> value.time() >= startTime && value.time() <= endTime)
                .groupBy((key, value) -> "agg-all-values")
                .windowedBy(TimeWindows.of(windowDuration))
                .aggregate(
                        MetricStats::new,
                        (key, value, aggregate) -> aggregate.add(value),
                        Materialized.with(new Serdes.StringSerde(), new MetricStatsSerde())
                );

        /**
         * accumulate the final value of each window threshold
         */
        windowedTaskStatsKTable.toStream().foreach((key, value) -> {
                    log.debug("Processing:{} time:{}", value, key.window().end());
                    processedLast = key.window().end();
                    if (currentWindowStats != null && key.window().end() != currentWindowStats.getTime()) {
                        log.debug("Next Window:{} time:{}", currentWindowStats, key.window().end());
                        stats.add(currentWindowStats);
                        if (stats.size() > RETENTION) {
                            stats.remove();
                        }
                    } else {
                        log.debug("Accumulate");
                    }
                    currentWindowStats = value;
                    currentWindowStats.setTime(key.window().end());
                }
        );
        return builder.build();
    }

    public void start() {
        streams = new KafkaStreams(topology, streamsConfig);
        streams.start();
    }

    public void stop() {
        streams.close();
        streams.cleanUp();
    }

    public List<MetricStats> getMetrics() {
        if (currentWindowStats != null && currentWindowStats.getTime() < System.currentTimeMillis() - (windowDuration * 1000)) {
            stats.add(currentWindowStats);
            currentWindowStats = null;
        } else if (currentWindowStats == null) {
            currentWindowStats = new MetricStats();
            currentWindowStats.setTime(System.currentTimeMillis() - (windowDuration * 1000));
        }
        CopyOnWriteArrayList results = new CopyOnWriteArrayList<>(stats);
        if (currentWindowStats != null) results.add(currentWindowStats);
        Collections.reverse(results);
        return results;
    }

    public Topology getTopology() {
        return topology;
    }

    public void waitUntilReady() {
        try {
            Thread.sleep(5000);
            int waited = 0;
            while (processedLast == 0 && waited++ < 2 && processedLast != 0 && processedLast < endTime) {
                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
