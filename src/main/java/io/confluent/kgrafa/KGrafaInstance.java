/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/AGPL-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kgrafa;

import io.confluent.kgrafa.model.metric.Metric;
import io.confluent.kgrafa.model.metric.MetricSerDes;
import io.confluent.kgrafa.util.KafkaTopicClientImpl;
import io.confluent.kgrafa.util.LockfreeConcurrentQueue;
import io.confluent.ksql.util.KsqlConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KGrafaInstance {

  private final KGrafa kgrafana;
    private final ScheduledExecutorService scheduler;
    Queue<Metric> inputMetrics = new LockfreeConcurrentQueue<>();

    public KGrafaInstance(KGrafa kgrafa) {
    this.kgrafana = kgrafa;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> flushMetrics(), 10, 1, TimeUnit.SECONDS);

  }

  public KGrafa getInstance() {
    return kgrafana;
  }

    synchronized public void flushMetrics() {
        KafkaProducer producer = getProducer();
        while (!inputMetrics.isEmpty()) {
            Metric metric = inputMetrics.remove();
            // TODO: fix partition assignment based upon topic partition count and hash/modulus of resource-key
            producer.send(new ProducerRecord(metric.getName(), 0, metric.getTime(), metric.getResource(), metric));

            System.out.println("Storing: " + metric);
        }
        producer.flush();
    }



  /**
   * Note: dont care about double locking because it is always created on startup in the Servlet Lifecycle.start()
   */
  private static KGrafaInstance singleton = null;

    public static KGrafaInstance getInstance(Properties propertes) {
    if (singleton == null) {

        if (propertes == null) {
            throw new RuntimeException("KGrafa has not been initialized! -= pass in valid properties and init before use");
        }
      KafkaTopicClientImpl topicClient = getKafkaTopicClient(propertes);

      Integer numPartitions = Integer.valueOf(propertes.getProperty("numPartitions", "3"));
      Short numReplicas = Short.valueOf(propertes.getProperty("numReplicas", "1"));

        SimpleKGrafa kgrafa = new SimpleKGrafa(
              topicClient,
              Integer.valueOf(propertes.getProperty("numPriorities", "9")),
              propertes.getProperty("prefix", "kgrafana"),
              propertes.getProperty("bootstrap.servers", "localhost:9092"),
              numPartitions, numReplicas
              );

        kgrafa.start();
        singleton = new KGrafaInstance(kgrafa);
        return singleton;
    }
    return singleton;
  }

  public static KafkaTopicClientImpl getKafkaTopicClient(Properties propertes) {
    Properties consumerConfig = new Properties();
    consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, propertes.getProperty("bootstrap.servers", "localhost:9092"));
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, propertes.getProperty("prefix", "kgrafana"));
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

    KsqlConfig ksqlConfig = new KsqlConfig(consumerConfig);

    Map<String, Object> ksqlAdminClientConfigProps = ksqlConfig.getKsqlAdminClientConfigProps();

    AdminClient adminClient = AdminClient.create(ksqlAdminClientConfigProps);
    return new KafkaTopicClientImpl(adminClient);
  }


    private KafkaProducer<String, Metric> producer;

    private synchronized KafkaProducer getProducer() {
        if (producer == null) {
            producer = new KafkaProducer<>(producerConfig(), new StringSerializer(), new MetricSerDes());
        }
        return producer;
    }

    private Properties producerConfig() {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"));
//    producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
        return producerConfig;
    }

    public int add(Metric metric) {
        inputMetrics.add(metric);
        if (inputMetrics.size() > 1000) {
            flushMetrics();
        }
        return inputMetrics.size();
    }
}
