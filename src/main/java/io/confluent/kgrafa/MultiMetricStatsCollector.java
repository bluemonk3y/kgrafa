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

import static io.confluent.kgrafa.model.metric.MetricStats.MetricStatsSerde;

/**
 * Pulls a single topics set of metrics
 */
public class MultiMetricStatsCollector {
    private static final Logger log = LoggerFactory.getLogger(MultiMetricStatsCollector.class);

    private final Topology topology;
    private final Properties streamsConfig;
    private long startTime;
    private long endTime;
    private KafkaStreams streams;
    private final Map<String, Map<Long, MetricStats>> stats = new TreeMap<>();
    private long windowDuration;
    private long processedLast = 0;


    // TODO: consider passing in metric filter for filtering against specific metric name
    public MultiMetricStatsCollector(final List<String> topics, final Properties streamsConfig, final long windowDuration, final long startTime, final long endTime) {
        this.streamsConfig = streamsConfig;
        this.startTime = startTime;
        this.endTime = endTime;
        this.windowDuration = windowDuration;

        this.topology = buildTopology(topics);

    }

    private Topology buildTopology(final List<String> metricTopic) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Metric> tasks = builder.stream(metricTopic);

        log.debug("Duration:" + windowDuration + "ms " + new Date(startTime) + " - " + new Date(endTime) + " Topic: " + metricTopic + " Window:" + windowDuration);

        KTable<Windowed<String>, MetricStats> windowedTaskStatsKTable = tasks
                .filter((key, value) -> value.getTime() >= startTime && value.getTime() <= endTime)
                .groupByKey()
                .windowedBy(TimeWindows.of(windowDuration))
                .aggregate(
                        MetricStats::new,
                        (key, value, aggregate) -> aggregate.add(value),
                        Materialized.with(new Serdes.StringSerde(), new MetricStatsSerde())
                );

        /**
         * Accumulate each window event internally by tracking the window as part of the aggregation
         */
        windowedTaskStatsKTable.toStream().foreach((key, metricStats) -> {
                    System.out.println("Agg:" + key.key());
                    processedLast = key.window().end();
                    stats.computeIfAbsent(key.key(), k -> new LinkedHashMap<>()).computeIfAbsent(key.window().end(), k -> metricStats.set(key.key(), key.window().end()));
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

    public List<List<MetricStats>> getMetrics() {
        List<List<MetricStats>> results = new ArrayList<>();
        for (Map.Entry<String, Map<Long, MetricStats>> entry : stats.entrySet()) {
            ArrayList<MetricStats> metricStats1 = new ArrayList<>(entry.getValue().values());
            Collections.reverse(metricStats1);
            results.add(metricStats1);
        }
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
