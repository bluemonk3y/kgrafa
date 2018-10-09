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

import io.confluent.kgrafa.utils.IntegrationTestHarness;
import org.junit.After;
import org.junit.Before;

public class SimpleKGrafaTaskPriorityTest {


  private IntegrationTestHarness testHarness;
  private SimpleKGrafa kgrafa;

  @Before
  public void before() throws Exception {
    testHarness = new IntegrationTestHarness();
    testHarness.start();

    kgrafa = new SimpleKGrafa(testHarness.getTopicClient(), 5, "KWQ", testHarness.embeddedKafkaCluster.bootstrapServers(), 1, (short)1);

    kgrafa.start();
  }

  @After
  public void after() {
    testHarness.stop();
  }


//
//  @Test
//  public void serviceSingleTask() throws Exception {
//
//    Task firstItem = TaskDataProvider.data.values().iterator().next();
//
//    testHarness.produceData("KWQ-5", Collections.singletonMap(firstItem.getId(), firstItem), new TaskSerDes(), 1L);
//
//    Task consume = kgrafa.consume();
//    Assert.assertEquals(firstItem, consume);
//    System.out.println(consume);
//  }


}