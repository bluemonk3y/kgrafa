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

import io.confluent.kgrafa.model.Query;
import io.confluent.kgrafa.model.Range;
import io.confluent.kgrafa.model.metric.Metric;
import io.confluent.kgrafa.model.metric.MetricStats;
import io.confluent.kgrafa.model.metric.MultiMetricStats;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Pulls a single topics set of metrics
 */
public class MultiMetricStatsCollector {
    private static final Logger log = LoggerFactory.getLogger(MultiMetricStatsCollector.class);

    private final Topology topology;
    private final Properties streamsConfig;
    private KafkaStreams streams;
    private final Map<String, Map<Long, MultiMetricStats>> stats = new TreeMap<>();
    private long windowDuration;

    private long processedLast = 0;
    private long firstWhen = 0;
    private long lastWhen = 0;
    private long queryEndTime;


    // TODO: consider passing in metric filter for filtering against specific metric name
    public MultiMetricStatsCollector(final Collection<String> topics, final Query query, final Properties streamsConfig, final long windowDuration) {
        this.streamsConfig = streamsConfig;
        this.windowDuration = windowDuration;

        this.topology = buildTopology(topics, query);

    }

    private Topology buildTopology(final Collection<String> metricTopic, Query query) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Metric> tasks = builder.stream(metricTopic);
        Range queryRange = query.getRange();
        queryEndTime = queryRange.getEnd();

        log.debug("Duration:" + windowDuration + "ms " + new Date(queryRange.getStart()) + " - " + new Date(queryRange.getEnd()) + " Topic: " + metricTopic + " Window:" + windowDuration);

        Materialized<String, MultiMetricStats, WindowStore<Bytes, byte[]>> ss1 = Materialized.with(new Serdes.StringSerde(), new MetricStats.MultiMetricStatsSerde());
        Materialized<String, MultiMetricStats, WindowStore<Bytes, byte[]>> ss2 = ss1.withLoggingDisabled().withCachingDisabled();

        // Note: The consumer-id is already positioned to the start time for all TopicPartitions.
        // this will mean it starts from the right offset - but will also rely on the filter as it goes past the end
        KTable<Windowed<String>, MultiMetricStats> windowedTaskStatsKTable = tasks
                .filter((key, value) -> query.passesFilter(value))
                .groupByKey()
                .windowedBy(TimeWindows.of(windowDuration))
                .aggregate(
                        MultiMetricStats::new,
                        (key, value, aggregate) -> aggregate.addIt(value),
                        ss2
                );

        /**
         * Accumulate each window event internally by tracking the window as part of the aggregation
         */
        windowedTaskStatsKTable.toStream().foreach((key, metricStats) -> {
                    processedLast = key.window().end();
            if (firstWhen == 0) {
                firstWhen = System.currentTimeMillis();
            }
            lastWhen = System.currentTimeMillis();
            //System.out.println("Processing" + " key:" + key.key() + " time:" + key.window().end() + " ---" + metricStats);
            Map<Long, MultiMetricStats> longMultiMetricStatsMap = stats.computeIfAbsent(key.key(), k -> new LinkedHashMap<>());
            longMultiMetricStatsMap.put(key.window().end(), metricStats);
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
        for (Map.Entry<String, Map<Long, MultiMetricStats>> mmEntry : stats.entrySet()) {
            ArrayList<MetricStats> metricStats = new ArrayList<>();

            for (Map.Entry<Long, MultiMetricStats> value : mmEntry.getValue().entrySet()) {
                value.getValue().stats.values().forEach(stat -> stat.setTime(value.getKey().longValue()));
                ArrayList<MetricStats> metricStats1 = new ArrayList<>(value.getValue().stats.values());
                metricStats.addAll(metricStats1);
            }
            results.add(metricStats);
        }
        return results;
    }

    public Topology getTopology() {
        return topology;
    }

    public void waitUntilReady() {
        try {
            long start = System.currentTimeMillis();
            while (!finishedProcessing() && !waitedLongEnough(start)) {
                Thread.sleep(100);
            }
            log.debug("DONE Waiting started:{} finished:{}  processedLast:{} finished:{} waited:{}", new Date(firstWhen), new Date(lastWhen), new Date(processedLast), finishedProcessing(), waitedLongEnough(start));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean waitedLongEnough(long startedWaiting) {
        return System.currentTimeMillis() > startedWaiting + 10000;
    }

    private boolean finishedProcessing() {
        return processedLast > queryEndTime - 10000;
    }
}
