package io.confluent.kgrafa.util;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TimeSeeker {

    private final KafkaConsumer kafkaConsumer;

    public TimeSeeker(KafkaConsumer kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    public void seek(final long timestamp, List<String> topics) {

        Map<TopicPartition, Long> timestampMap = topics.stream().flatMap(topic -> getPartitions(topic).stream()).collect(Collectors.toMap(e -> e, e -> timestamp));

        Map<TopicPartition, OffsetAndTimestamp> rawOffsets = kafkaConsumer.offsetsForTimes(timestampMap);

        // topic partitions without data will have a null OffsetAndTimestamp
        Stream<Map.Entry<TopicPartition, OffsetAndTimestamp>> nonNullOffsets = rawOffsets.entrySet().stream().filter(entry -> entry.getValue() != null);
        Map<TopicPartition, OffsetAndMetadata> offsets = nonNullOffsets.collect(Collectors.toMap(e -> e.getKey(), e -> new OffsetAndMetadata(e.getValue().offset())));
        kafkaConsumer.commitSync(offsets);
    }

    private List<TopicPartition> getPartitions(String topic) {
        final List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(topic);
        return partitionInfos.stream().map(pi -> new TopicPartition(pi.topic(), pi.partition())).collect(Collectors.toList());
    }
}
