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

import io.confluent.kgrafa.model.*;
import io.confluent.kgrafa.model.metric.Metric;
import io.confluent.kgrafa.model.metric.MetricSerDes;
import io.confluent.kgrafa.model.metric.MetricStats;
import io.confluent.kgrafa.model.search.Target;
import io.confluent.kgrafa.util.KafkaTopicClientImpl;
import io.confluent.ksql.util.KsqlConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Handles
 * - testDatasource() used by datasource configuration page to make sure the connection is working
 * - query(options) used by panels to get data
 * - annotationQuery(options) used by dashboards to get annotations
 * - metricFindQuery(options)  used by query editor to get metric suggestions.
 * <p>
 * Javascript controls for the panel:
 * https://github.com/grafana/grafana/blob/master/docs/sources/plugins/developing/datasources.md
 * - QueryCtrl
 * - ConfigCtrl
 * - AnnotationsQueryCtrl
 * <p>
 * Another one is:
 * https://github.com/grafana/simple-json-datasource
 */
@Path("kgrafa")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class KGrafaResource {
    private static final Logger log = LoggerFactory.getLogger(KGrafaResource.class);

    static String metricPrefix = "metrics";
    private final KGrafaInstance instance;

    public KGrafaResource() {
        instance = KGrafaInstance.getInstance(null);
    }

    /**
     * Grafana Datasource API
     *
     * @return
     */


    @POST
    @Path("/search")
    @Operation(summary = "used by panels to get field names i.e. upper_25 etc",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String search(@Parameter(description = "query sent from the dashboard", required = true) Target query) {

        // 1 - start with metrics prefix,
        // 2 - check for existence of other specified tags

        List<String> topics = instance.getInstance().listTopics(new String[]{metricPrefix, query.getTarget()});

        StringBuilder results = new StringBuilder("[");
        for (int i = 0; i < topics.size(); i++) {
            results.append("\"").append(topics.get(i)).append("\"");

            if (i < topics.size() - 1) results.append(",");
        }
        results.append("]");
//      String v1 = "[ \"upper_25\"]";
        // moxy bug doesnt handle String[] type
        return results.toString();


    }

    @POST
    @Path("/annotations")
    @Operation(summary = "used by panels to get data",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })

    public String annotations(@Parameter(description = "query sent from the dashboard", required = true) String quary) {
        return "";
    }


    /**
     * Expects: {
     * "range": { "from": "2016-03-04T04:07:55.144Z", "to": "2016-03-04T07:07:55.144Z" },
     * "rangeRaw": { "from": "now-3h", to: "now" },
     * "annotation": {
     * "datasource": "generic datasource",
     * "enable": true,
     * "name": "annotation name"
     * }
     * }
     * Returns:
     * [
     * {
     * "annotation": {
     * "name": "annotation name", //should match the annotation name in grafana
     * "enabled": true,
     * "datasource": "generic datasource",
     * },
     * "title": "Cluster outage",
     * "time": 1457075272576,
     * "text": "Joe causes brain split",
     * "tags": "joe, cluster, failure"
     * }
     * ]
     */
    @POST
    @Path("/annotationQuery")
    @Operation(summary = "used by dashboards to get annotations",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = AnnnotationResult[].class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public AnnnotationResult[] annotationQuery(
            @Parameter(description = "used by dashboards to get annotations", required = true) AnnnotationQuery annotationQuery) {

        Range range = annotationQuery.getRange();
        RangeRaw rangeRaw = annotationQuery.getRangeRaw();
        Annnotation annotation = annotationQuery.getAnnnotation();


        AnnnotationResult annnotationResult = new AnnnotationResult();
        annnotationResult.setAnnotation(annotationQuery.getAnnotation());
        annnotationResult.setTime(System.currentTimeMillis());
        annnotationResult.setText("This is an annotation");
        annnotationResult.setTitle("Annotation Title");
        annnotationResult.setTags("Brain split statistics");
        return new AnnnotationResult[]{annnotationResult};
    }


    @POST
    @Path("/testDatasource")
    @Operation(summary = "used by datasource configuration page to make sure the connection is working",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String testDatasource() {
        return "{ status: \"success\", message: \"Datasource is working\", title: \"Success\" }";
    }


    /**
     * New one!
     * { panelId: 2,
     * range:
     * { from: '2018-11-15T07:13:32.496Z',
     * to: '2018-11-15T13:13:32.496Z',
     * raw: { from: 'now-6h', to: 'now' } },
     * rangeRaw: { from: 'now-6h', to: 'now' },
     * interval: '15s',
     * intervalMs: 15000,
     * targets: [ { target: 'upper_50', refId: 'A', type: 'timeserie' } ],
     * format: 'json',
     * maxDataPoints: 1430,
     * scopedVars:
     * { __interval: { text: '15s', value: '15s' },
     * __interval_ms: { text: 15000, value: 15000 } },
     * adhocFilters: [] }
     * <p>
     * <p>
     * <p>
     * {
     * "range": { "from": "2015-12-22T03:06:13.851Z", "to": "2015-12-22T06:48:24.137Z" },
     * "interval": "5s",
     * "targets": [
     * { "refId": "B", "target": "upper_75" },
     * { "refId": "A", "target": "upper_90" }
     * ],
     * "format": "json",
     * "maxDataPoints": 2495 //decided by the panel
     * }
     * Out {
     * "target":"upper_75",
     * "datapoints":[
     * [622, 1450754160000],
     * [365, 1450754220000]
     * ]
     * },
     */

    @POST
    @Path("/query")
    @Operation(summary = "used by panels to get data",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String query(@Parameter(description = "query sent from the dashboard", required = true) Query query) {

        log.debug("Got:{}", query);
        try {
            long startTime = System.currentTimeMillis();
            MultiMetricStatsCollector metricStatsCollector = new MultiMetricStatsCollector(query.getTargetsAsList(), streamsProperties(), query.getIntervalAsMillis(), query.getRange().getStart(), query.getRange().getEnd());

            metricStatsCollector.start();
            metricStatsCollector.waitUntilReady();
            metricStatsCollector.stop();

            ArrayList<TimeSeriesResult> results = new ArrayList<>();

            List<List<MetricStats>> metrics = metricStatsCollector.getMetrics();

            for (List<MetricStats> metric : metrics) {
                TimeSeriesResult timeSeriesResult = new TimeSeriesResult();
                timeSeriesResult.setValues(metric.get(0).getName(), metric);
                log.debug("TimeSeries Metric:{} \tpoints:{}", metric.get(0).getName(), metric.size());
                results.add(timeSeriesResult);
            }

            log.debug("Completed in time:{}", System.currentTimeMillis() - startTime);

            // moxy doesnt support multi-dimensional arrays so drop back to a json-string and rely on json response type
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=389815
            return results.toString();

        } catch (Throwable t) {
            t.printStackTrace();
            return "";
        }
    }

    @POST
    @Path("/metricFindQuery")
    @Operation(summary = "used by panels to get data",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String metricFindQuery(@Parameter(description = "query sent from the dashboard", required = true) String query) {
        return "";
    }


    /**
     * General API stuff
     *
     * @return
     */
    @GET
    @Produces("application/json")
    @Path("/info")
    public String info() {
        return "KGrafana Metrics Service";
    }

    @GET
    @Produces("application/json")
    @Path("/datasources")
    public String datasources() {


        List<String> topics = new ArrayList<>(instance.getInstance().listTopics(new String[]{metricPrefix, ""}));
        Collections.sort(topics);

//        return "{" +
//                "\n {\n" +
//                "  \"name\":\"test_datasource\",\n" +
//                "  \"type\":\"kgrafa\",\n" +
//                "  \"url\":\"http://mydatasource.com\",\n" +
//                "  \"access\":\"proxy\",\n" +
//                "  \"basicAuth\":false\n" +
//                " }\n" +
//                " \"sources\": \n" +
//                topics.toString() +
//                "\n}";
        System.out.println(topics);
        return topics.toString();
    }


    /**
     * Test data driver
     *
     * @return
     */
    @GET
    @Produces("application/json")
    @Path("/generateRandomData")
    public String generateRandomData() {
        String[] sources = {"emea-dc1"};
        String[] resources = {"apollo", "nasa", "mars", "saturn-5", "lander-6", "mission-12", "apoloco"};
        String[] metrics = {"cpu", "network", "latency"};

        for (String metric : metrics) {
            for (String resource : resources) {
                generateTestData(new GenerateRequest(sources[0], resource, metric, 60));
            }
        }

        return "{" +
                " \"response\":\"done\"\n" +
                "\n}";

    }


    @POST
    @Produces("application/json")
    @Path("/generateTestData")
    public String generateTestData(@Parameter(description = "source host", required = true) GenerateRequest request) {

        int duration = 5;
        long durationMs = duration * 60 * 1000;
        long endTime = System.currentTimeMillis();
        long startTime = endTime - durationMs;
        long interval = 1000;


        int i = 100;
        for (long t = startTime; t < endTime; t += interval) {
            double value = Math.random() * t / (1000 * 1000 * 1000);
            value += i * 100;
            Metric metric = new Metric(metricPrefix, request.getSource(), request.getResource(), request.getMetric(), value, t);
            putMetric(metric);
            i++;
        }

        return "{" +
                " \"response\":\"done\"\n" +
                "\n}";
    }

    @POST
    @Produces("application/json")
    @Path("/putMetric")
    public String putMetric(@Parameter(description = "source host", required = true) Metric metric) {
        int size = instance.add(metric);
        return String.format("{ \"queue\": \"%d\"}", size);
    }


    private KafkaTopicClientImpl kafkaTopicClient;


    synchronized public KafkaTopicClientImpl getKafkaTopicClient() {
        if (kafkaTopicClient == null) {
            Properties consumerConfig = new Properties();
            consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"));
            consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, System.getProperty("prefix", "kgrafa"));
            consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

            KsqlConfig ksqlConfig = new KsqlConfig(consumerConfig);

            Map<String, Object> ksqlAdminClientConfigProps = ksqlConfig.getKsqlAdminClientConfigProps();

            AdminClient adminClient = AdminClient.create(ksqlAdminClientConfigProps);
            kafkaTopicClient = new KafkaTopicClientImpl(adminClient);
        }
        return kafkaTopicClient;
    }


    private Properties streamsProperties() {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "kgrafa-server-" + System.currentTimeMillis());
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"));
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MetricSerDes.class);
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
        config.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class.getName());

        return config;
    }
}
