package io.confluent.kgrafa;

import io.confluent.kgrafa.model.Query;
import io.confluent.kgrafa.model.Range;
import io.confluent.kgrafa.model.Target;
import io.confluent.kgrafa.model.metric.Metric;
import io.confluent.kgrafa.model.metric.MetricSerDes;
import io.confluent.kgrafa.model.metric.MetricStats;
import io.confluent.kgrafa.utils.MetricDataProvider;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.assertEquals;

public class MetricStatsCollectorTest {

    @Test
    public void multiTopicTotalWindowEventsForOneSecBucket() throws Exception {
        processMultiMetricTopic(new String[]{"topic-1", "topic-2"}, 1000, 1, 6, 1000, 7000);
    }


    @Test
    public void singleTopicTotalWindowEventsForOneSecBucket() throws Exception {
        processMultiMetricTopic(new String[]{"/path/one"}, 1000, 1, 6, 1000, 7000);
    }

    @Test
    public void multiTopicTotalWindowEventsForTenSecBucket() throws Exception {
        processMultiMetricTopic(new String[]{"/path/one", "/path/two"}, 10000, 3, 1, 3000, 10000);
    }

    @Test
    public void singleTopicTotalWindowEventsForTenSecBucket() throws Exception {
        processMultiMetricTopic(new String[]{"/path/one"}, 10000, 3, 1, 3000, 10000);
    }

    private void processMultiMetricTopic(String[] topics, int windowDuration, int metricsPerFirstStat, int totalBuckets, int timeIncrement, int timeMax) throws Exception {

        Properties streamsConfig = getProperties("localhost:9091");


        Query query = new Query();
        query.setTargets(new Target[]{new Target("ref-1", "/path/one cpu avg")});
        query.setRange(new Range());

        MultiMetricStatsCollector metricStatsCollector = new MultiMetricStatsCollector(Arrays.asList(topics), query, streamsConfig, windowDuration);

        Topology topology = metricStatsCollector.getTopology();

        TopologyTestDriver driver = new TopologyTestDriver(topology, streamsConfig);

        Map<String, Metric> sourceData = MetricDataProvider.data(timeIncrement, timeMax);

        int i = 0;

        Set<String> metrics = new HashSet<>();
        MetricSerDes serDes = new MetricSerDes();
        for (Metric entry : sourceData.values()) {
            for (String topic : topics) {
                System.out.println("TTTTT Publishing:" + entry.getKey() + " time:" + entry.time() + " entry: " + entry);
                ConsumerRecord consumerRecord = new ConsumerRecord(topic, 0, i, entry.time(), TimestampType.CREATE_TIME, 1, 1, 1, entry.getKey().getBytes(), serDes.serialize("", entry));
                driver.pipeInput(consumerRecord);
                metrics.add(entry.getName());

            }
        }
        driver.close();

        List<List<MetricStats>> metricStats = metricStatsCollector.getMetrics();

        assertEquals(metrics.size(), metricStats.size());
        MetricStats next = metricStats.get(0).get(0);
        assertEquals(next.toString(), metricsPerFirstStat, next.getTotal());
        assertEquals(metricStats.get(0).toString(), totalBuckets, metricStats.get(0).size());

    }

    private Properties getProperties(String broker) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "TEST-APP-ID");// + System.currentTimeMillis());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MetricSerDes.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2000);
        return props;
    }
}
