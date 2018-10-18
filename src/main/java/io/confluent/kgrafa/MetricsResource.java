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

import io.confluent.kgrafa.model.Annnotation;
import io.confluent.kgrafa.model.AnnnotationQuery;
import io.confluent.kgrafa.model.AnnnotationResult;
import io.confluent.kgrafa.model.GenerateRequest;
import io.confluent.kgrafa.model.Query;
import io.confluent.kgrafa.model.Range;
import io.confluent.kgrafa.model.RangeRaw;
import io.confluent.kgrafa.model.TimeSeriesResult;
import io.confluent.kgrafa.model.metric.Metric;
import io.confluent.kgrafa.model.metric.MetricSerDes;
import io.confluent.kgrafa.util.KafkaTopicClientImpl;
import io.confluent.kgrafa.util.LockfreeConcurrentQueue;
import io.confluent.ksql.util.KsqlConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Handles
 * - testDatasource() used by datasource configuration page to make sure the connection is working
 * - query(options) used by panels to get data
 * - annotationQuery(options) used by dashboards to get annotations
 * - metricFindQuery(options)  used by query editor to get metric suggestions.
 *
 * Javascript controls for the panel:
 * https://github.com/grafana/grafana/blob/master/docs/sources/plugins/developing/datasources.md
 * - QueryCtrl
 * - ConfigCtrl
 * - AnnotationsQueryCtrl
 *
 * Another one is:
 * https://github.com/grafana/simple-json-datasource
 *
 */
@Path("metrics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class MetricsResource {
  Queue<Metric> inputMetrics = new LockfreeConcurrentQueue<>();

  private final ScheduledExecutorService scheduler;
  String metricPrefix = "metrics";

  public MetricsResource() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> flushMetrics(), 10, 10, TimeUnit.SECONDS);
  }

  /**
   * Grafana Datasource API
   * @return
   */

  /**
   *
   * Expects: {
   *   "range": { "from": "2016-03-04T04:07:55.144Z", "to": "2016-03-04T07:07:55.144Z" },
   *   "rangeRaw": { "from": "now-3h", to: "now" },
   *   "annotation": {
   *     "datasource": "generic datasource",
   *     "enable": true,
   *     "name": "annotation name"
   *   }
   * }
   * Returns:
   * [
   *   {
   *     "annotation": {
   *       "name": "annotation name", //should match the annotation name in grafana
   *       "enabled": true,
   *       "datasource": "generic datasource",
   *      },
   *     "title": "Cluster outage",
   *     "time": 1457075272576,
   *     "text": "Joe causes brain split",
   *     "tags": "joe, cluster, failure"
   *   }
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
          @Parameter(description = "used by dashboards to get annotations" , required = true)  AnnnotationQuery annotationQuery) {

    Range range = annotationQuery.getRange();
    RangeRaw rangeRaw = annotationQuery.getRangeRaw();
    Annnotation annotation = annotationQuery.getAnnnotation();


    AnnnotationResult annnotationResult = new AnnnotationResult();
    annnotationResult.setAnnotation(annotationQuery.getAnnotation());
    annnotationResult.setTime(System.currentTimeMillis());
    annnotationResult.setText("This is an annotation");
    annnotationResult.setTitle("Annotation Title");
    annnotationResult.setTags("Brain split statistics");
    return new AnnnotationResult[] { annnotationResult} ;
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
    return  "{ status: \"success\", message: \"Data source is working\", title: \"Success\" }";
  }


  /**
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
  public String query(@Parameter(description = "query sent from the dashboard" , required = true) Query query ) {

    MetricStatsCollector metricStatsCollector = new MetricStatsCollector(query.getTargets()[0].getTarget(), streamsProperties(), query.getIntervalAsMillis(), query.getRange().getStart(), query.getRange().getEnd());

    metricStatsCollector.start();
    metricStatsCollector.waitUntilReady();

    ArrayList<TimeSeriesResult> results = new ArrayList<>();

    TimeSeriesResult timeSeriesResult = new TimeSeriesResult();
    timeSeriesResult.setTarget(query.getTargets()[0].getTarget());
    timeSeriesResult.setValues(metricStatsCollector.getMetrics());
    results.add(timeSeriesResult);

    // moxy doesnt support multi-dimensional arrays so drop back to a json-string and rely on json response type
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=389815
    return results.toString();
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

  @POST
  @Path("/search")
  @Operation(summary = "used by panels to get data",
          tags = {"query"},
          responses = {
                  @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                  @ApiResponse(responseCode = "405", description = "Invalid input")
          })
  public String search(@Parameter(description = "query sent from the dashboard", required = true) String query) {
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


    Set<String> raw = getKafkaTopicClient().listTopicNames();

    List<String> datasources = new ArrayList<>(raw.stream().filter(i -> i.contains(metricPrefix)).collect(Collectors.toList()));

    Collections.sort(datasources);


    return "{" +
            "\n {\n" +
            "  \"name\":\"test_datasource\",\n" +
            "  \"type\":\"kgrafa\",\n" +
            "  \"url\":\"http://mydatasource.com\",\n" +
            "  \"access\":\"proxy\",\n" +
            "  \"basicAuth\":false\n" +
            " }\n" +
            " list of available sources\": \n" +
            datasources.toString() +
            "\n}";
  }


  /**
   * Test data driver
   * @return
   */
  @POST
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

    int minutes = 5;
    int points = 60 * minutes;
    long time = System.currentTimeMillis() - 1000l * points;
    // data for every 1 second
    long interval = System.currentTimeMillis() - time / 1000;
    for (int i = 0; i < points; i++) {
      double value = (Math.random() * 100.0) * i + 1; // make a rough upward trend
      time += interval;
      putMetric(new Metric(metricPrefix, request.getSource(), request.getResource(), request.getMetric(), value, time));
    }

    return "{" +
            " \"response\":\"done\"\n" +
            "\n}";
  }

  @POST
  @Produces("application/json")
  @Path("/putMetric")
  public String putMetric(@Parameter(description = "source host", required = true) Metric metric) {
    inputMetrics.add(metric);
    if (inputMetrics.size() > 1000) {
      flushMetrics();
    }
    return String.format("{ \"queue\": \"%d\"", inputMetrics.size());
  }

  synchronized private void flushMetrics() {
    KafkaProducer producer = getProducer();
    while (!inputMetrics.isEmpty()) {
      Metric metric = inputMetrics.remove();
      producer.send(new ProducerRecord(metric.getName(), metric.getResource(), metric));
    }
    producer.flush();
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

  private StreamsConfig streamsProperties() {
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "kgrafa-server");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"));
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MetricSerDes.class);
    config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5000);
    return new StreamsConfig(config);
  }
}
