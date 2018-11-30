/**
 * Copyright 2018 Confluent Inc.
 * <p>
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/AGPL-3.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kgrafa;

import io.confluent.kgrafa.util.KafkaTopicClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SimpleKGrafa uses the requesting thread to service the next ConsumerAndRecords[] set iterator.
 * Note: This impl relies on auto-commit; as a result - if killed, it can potentially lose messages that were held in-memory, but not yet dispatched.
 */
public class SimpleKGrafa implements KGrafa {


    private final int numPartitions;
    private final short replicationFactor;

    private final KafkaTopicClient topicClient;

    SimpleKGrafa(KafkaTopicClient topicClient, int numPartitions, short replicationFactor) {
        this.numPartitions = numPartitions;
        this.replicationFactor = replicationFactor;
        this.topicClient = topicClient;
    }


    @Override
    public String status() {
        return "running... yay";
    }

    @Override
    public List<String> listTopics(String[] filters) {
        Set<String> topics = topicClient.listTopicNames();
        List<String> collect = topics.stream().filter(topic -> {
            int hits = 0;
            for (String filter : filters) {
                if (topic.contains(filter)) hits++;
            }
            return hits == filters.length;
        }).collect(Collectors.toList());

        return collect;
    }

    public void createTopic(String topic) {
        topicClient.createTopic(topic, numPartitions, replicationFactor);
    }

    public int getNumPartitions() {
        return numPartitions;
    }

}
