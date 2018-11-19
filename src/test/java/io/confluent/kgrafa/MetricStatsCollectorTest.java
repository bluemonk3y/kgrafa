package io.confluent.kgrafa;

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
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MetricStatsCollectorTest {

    @Test
    public void getTotalWindowEventsForOneSecBucket() throws Exception {
        process(1000, 1, 6);
    }

    @Test
    public void getTotalWindowEventsForTenSecBucket() throws Exception {
        process(10000, 6, 1);
    }

    public void process(int windowDuration, int metricsPerFirstStat, int totalBuckets) throws Exception {

        Properties streamsConfig = getProperties("localhost:9091");


        MetricStatsCollector metricStatsCollector = new MetricStatsCollector("TestTopic", streamsConfig, windowDuration, 0, System.currentTimeMillis());

        Topology topology = metricStatsCollector.getTopology();

        TopologyTestDriver driver = new TopologyTestDriver(topology, streamsConfig);

        Map<String, Metric> sourceData = MetricDataProvider.data;


        int i = 0;
        MetricSerDes serDes = new MetricSerDes();
        for (Map.Entry<String, Metric> entry : sourceData.entrySet()) {
            ConsumerRecord consumerRecord = new ConsumerRecord("TestTopic", 0, i, entry.getValue().getTime(), TimestampType.CREATE_TIME, 1, 1, 1, entry.getKey().getBytes(), serDes.serialize("", entry.getValue()));
            driver.pipeInput(consumerRecord);
        }
        driver.close();

        List<MetricStats> metricStats = metricStatsCollector.getMetrics();

        // read the current throughput -= should be 1
        MetricStats next = metricStats.iterator().next();
        System.out.println("GOT:::" + metricStats.size());
        Assert.assertEquals(next.toString(), metricsPerFirstStat, next.getTotal());
        Assert.assertEquals(totalBuckets, metricStats.size());

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
