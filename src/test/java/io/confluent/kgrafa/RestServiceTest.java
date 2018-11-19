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

import io.confluent.kgrafa.utils.IntegrationTestHarness;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class RestServiceTest {


    private IntegrationTestHarness testHarness;

    @Before
    public void before() throws Exception {
        testHarness = new IntegrationTestHarness();
        testHarness.start();

        System.setProperty("bootstrap.servers", testHarness.embeddedKafkaCluster.bootstrapServers());

        System.setProperty("kgrafana.resources.folder", "src/main/resources");


        Thread.sleep(500);

        //    testHarness.createTopic("metrics_nasa-6_cpu", 1, 1);
        //    testHarness.createTopic("metrics_prometheus_cpu", 1, 1);
        //    testHarness.createTopic("metrics_apollo_cpu", 1, 1);

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

        generateRandomData();
        Thread.sleep(30 * 60 * 1000);
    }

    private void generateRandomData() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080").path("/kgrafa/generateRandomData");
        target.request(MediaType.APPLICATION_JSON).get();

    }

    @Test
    public void testAutogenDataIsSearchable() throws Exception {

        Client client = ClientBuilder.newClient();
        String datasources = client.target("http://localhost:8080").path("/kgrafa/datasources")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        System.out.println("Sources:" + datasources);

        String autogenMetric = "metrics_emea-dc1_apollo_cpu";
        /**
         * Query the time series data
         */

        WebTarget tsTarget = client.target("http://localhost:8080").path("/kgrafa/query");

        String query = String.format("{\n" +
                        "  \"range\": {\n" +
                        "    \"from\": \"%s\",\n" +
                        "    \"to\": \"%s\",\n" +
                        "    \"end\": 0,\n" +
                        "    \"start\": 0,\n" +
                        "    \"duration\": 1000\n" +
                        "  },\n" +
                        "  \"interval\": \"1000\",\n" +
                        "  \"targets\": [\n" +
                        "    {\n" +
                        "      \"refId\": \"A\",\"target\": \"%s\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"format\": \"string\",\n" +
                        "  \"maxDataPoints\": 100,\n" +
                        "  \"intervalAsMillis\": 100\n" +
                        "}",
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(0),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()),
                autogenMetric);

        String response =
                tsTarget.request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity(query, MediaType.APPLICATION_JSON),
                                String.class);


        System.out.println(response);

        Thread.sleep(10 * 10000);


    }

    @Test
    public void testDataGeneratedCanBeSearchTag() throws Exception {


        // /kgrafa/putMetric
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080").path("/kgrafa/putMetric");

        String form = "{\n" +
                "  \"name\": \"metrics_server-1\",\n" +
                "  \"resource\": \"server-1\",\n" +
                "  \"value\": %d,\n" +
                "  \"time\": %d\n" +
                "}";
        for (int i = 0; i < 100; i++) {
            String payload = String.format(form, i, System.currentTimeMillis() - (i * 1000));

            String response =
                    target.request(MediaType.APPLICATION_JSON_TYPE)
                            .post(Entity.entity(payload, MediaType.APPLICATION_JSON),
                                    String.class);

            System.out.println(response);
            assertThat(response, CoreMatchers.containsString("queue"));

        }


        /**
         * Check the data source was created - wait for the queue flush
         */
        Thread.sleep(10000);

        String datasources = client.target("http://localhost:8080").path("/kgrafa/datasources")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);


        assertThat(datasources, containsString("metrics_server-1"));

        /**
         * Query the time series data
         */

        WebTarget tsTarget = client.target("http://localhost:8080").path("/kgrafa/query");

        String query = String.format("{\n" +
                        "  \"range\": {\n" +
                        "    \"from\": \"%s\",\n" +
                        "    \"to\": \"%s\",\n" +
                        "    \"end\": 0,\n" +
                        "    \"start\": 0,\n" +
                        "    \"duration\": 1000\n" +
                        "  },\n" +
                        "  \"interval\": \"1000\",\n" +
                        "  \"targets\": [\n" +
                        "    {\n" +
                        "      \"refId\": \"A\",\"target\": \"%s\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"format\": \"string\",\n" +
                        "  \"maxDataPoints\": 100,\n" +
                        "  \"intervalAsMillis\": 100\n" +
                        "}",
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(0),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()),
                "metrics_server-1");

        String response =
                tsTarget.request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity(query, MediaType.APPLICATION_JSON),
                                String.class);

        System.out.println(":::::::" + response);
    }
}
