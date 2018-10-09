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

import io.confluent.ksql.util.KsqlConfig;
import io.confluent.kgrafa.util.KafkaTopicClientImpl;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Map;
import java.util.Properties;

public class KGrafanaInstance {

  private final KGrafa kgrafana;

  public KGrafanaInstance(KGrafa kgrafa) {
    this.kgrafana = kgrafa;
  }

  public KGrafa getInstance() {
    return kgrafana;
  }



  /**
   * Note: dont care about double locking because it is always created on startup in the Servlet Lifecycle.start()
   */
  static KGrafanaInstance singleton = null;
  public static KGrafanaInstance getInstance(Properties propertes) {
    if (singleton == null) {

      KafkaTopicClientImpl topicClient = getKafkaTopicClient(propertes);

      Integer numPartitions = Integer.valueOf(propertes.getProperty("numPartitions", "3"));
      Short numReplicas = Short.valueOf(propertes.getProperty("numReplicas", "1"));

      SimpleKGrafa kwq = new SimpleKGrafa(
              topicClient,
              Integer.valueOf(propertes.getProperty("numPriorities", "9")),
              propertes.getProperty("prefix", "kgrafana"),
              propertes.getProperty("bootstrap.servers", "localhost:9092"),
              numPartitions, numReplicas
              );

      kwq.start();
      singleton = new KGrafanaInstance(
              kwq
//              ,
//                new TaskStatusImpl(propertes.getProperty("bootstrap.servers", "localhost:9092"), topicClient, numPartitions, numReplicas)
        );
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

}
