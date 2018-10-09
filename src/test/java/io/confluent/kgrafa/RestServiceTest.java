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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestServiceTest {


  private IntegrationTestHarness testHarness;

  @Before
  public void before() throws Exception {
    testHarness = new IntegrationTestHarness();
    testHarness.start();

    System.setProperty("bootstrap.servers", testHarness.embeddedKafkaCluster.bootstrapServers());

    System.setProperty("kgrafana.resources.folder", "src/main/resources");



    Thread.sleep( 500);

    RestServerMain.initialize();
    RestServerMain.start();
  }

  @After
  public void after() {
    RestServerMain.stop();
    RestServerMain.destroy();
    testHarness.stop();
  }


  @Test
  public void runServerForAbit() throws Exception {
    Thread.sleep( 30 * 60 * 1000);
  }


  @Test
  public void runSimulation() throws Exception {

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target("http://localhost:8080").path("/kwq/simulate/{numberOfTasks}/{durationSeconds}/{numberOfWorkers}");
    target = target.resolveTemplate("numberOfTasks", "500");
    target = target.resolveTemplate("durationSeconds", "1");
    target = target.resolveTemplate("numberOfWorkers", "50");
    Response put = target.request(MediaType.APPLICATION_JSON_TYPE).get();

    Assert.assertNotNull("Should have created KWQ instance", put);
  }
  @Test
  public void testGetTask() throws Exception {

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:8080").path("/kwq");
    String response = target.request(MediaType.APPLICATION_JSON_TYPE).get(String.class);

    Assert.assertNotNull("Should have created KSWQ and returned a valid string", response);
    assertThat(response, containsString("io.confluent.kwq.SimpleKGrafa@"));
  }
}