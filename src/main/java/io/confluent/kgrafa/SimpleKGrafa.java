/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kgrafa;

import io.confluent.kgrafa.util.KafkaTopicClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * SimpleKGrafa uses the requesting thread to service the next ConsumerAndRecords[] set iterator.
 * Note: This impl relies on auto-commit; as a result - if killed, it can potentially lose messages that were held in-memory, but not yet dispatched.
 */
public class SimpleKGrafa implements KGrafa {
  private static final int CONSUMER_POLL_TIMEOUT_MS = 100;
  private static final long IDLE_WAIT_MS = 1000L;

  private final int numPriorities;
  private final String prefix;
  private String bootstrapServers;
  private final int numPartitions;
  private final short replicationFactor;
  private final List<String> topics = new ArrayList<>();
  private final List<KafkaConsumer> consumers = new ArrayList<>();
  private KafkaProducer producer = null;

  private final KafkaTopicClient topicClient;

  public SimpleKGrafa(KafkaTopicClient topicClient, int numPriorities, String prefix, String bootstrapServers, int numPartitions, short replicationFactor) {
    this.numPriorities = numPriorities;
    this.prefix = prefix.toUpperCase();
    this.bootstrapServers = bootstrapServers;
    this.numPartitions = numPartitions;
    this.replicationFactor = replicationFactor;
    this.topicClient = topicClient;
  }


  public void start() {
  }

  @Override
  public void pause() {
  }

  @Override
  public String status() {
    return "running... yay";
  }



  private Properties consumerConfig(String bootstrapServers) {
    Properties config = new Properties();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, prefix);
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return config;
  }

  private Properties producerConfig(String bootstrapServers) {
    Properties producerConfig = new Properties();
    producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
    producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
    return producerConfig;
  }

}
