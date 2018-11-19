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
package io.confluent.kgrafa.model.metric;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class MetricSerDes implements Serde<Metric>, Serializer<Metric>, Deserializer<Metric> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public Metric deserialize(String s, byte[] bytes) {
    try {
      if (bytes == null) {
        throw new RuntimeException(this.getClass().getSimpleName() + ": Cannot read 'null' record for key:" + s);
      }
      JsonNode jsonNode = this.objectMapper.readTree(bytes);
        Metric metric = new Metric(null, "", jsonNode.get("resource").asText(), null, jsonNode.get("value").asDouble(), jsonNode.get("time").asLong());
        metric.setName(jsonNode.get("name").asText());
        System.out.println("RECORD:" + metric);
        return metric;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void configure(Map<String, ?> map, boolean b) {

  }

  public byte[] serialize(String s, Metric task) {
    try {
      return this.objectMapper.writeValueAsBytes(task);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {

  }

  @Override
  public Serializer<Metric> serializer() {
    return this;
  }

  @Override
  public Deserializer<Metric> deserializer() {
    return this;
  }
}
