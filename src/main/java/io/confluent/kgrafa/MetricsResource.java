package io.confluent.kgrafa;

import io.confluent.kgrafa.model.Annnotation;
import io.confluent.kgrafa.model.AnnnotationQuery;
import io.confluent.kgrafa.model.AnnnotationResult;
import io.confluent.kgrafa.model.Query;
import io.confluent.kgrafa.model.Range;
import io.confluent.kgrafa.model.RangeRaw;
import io.confluent.kgrafa.model.Target;
import io.confluent.kgrafa.model.TimeSeriesResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;

/**
 * Handles
 * - testDatasource() used by datasource configuration page to make sure the connection is working
 * - query(options) used by panels to get data
 * - annotationQuery(options) used by dashboards to get annotations
 * - metricFindQuery(options)  used by query editor to get metric suggestions.
 *
 */
@Path("metrics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class MetricsResource {

  @GET
  @Produces("application/json")
  @Path("/info")
  public String get() {
    return "KGrafana Metrics Service";
  }

  @OPTIONS
  @Path("/annotations")
  public Response annotationOptions() {

    return noContent();
  }

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

  @OPTIONS
  @Path("/search")
  public Response searchOptions() {
    return noContent();
  }

  private Response noContent() {
    return Response.noContent()
            .header("Access-Control-Allow-Headers", "accept, content-type")
            .header("Access-Control-Allow-Methods", "POST")
            .header("Access-Control-Allow-Origin", "*")
            .build();
  }

//  @POST
//  @Path("/search")
//  public JsonArray search(
//          @Parameter(description =
//                  " <br> " , required = true) JsonObject query) {
//
//    String sq = query.toString();
//    System.out.println(sq);
//
//    JsonArrayBuilder arr = Json.createArrayBuilder();
//    arr.add("upper_75");
//    arr.add("upper_80");
//    arr.add("upper_90");
//    return arr.build();
//  }

  // TODO
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


  @OPTIONS
  @Path("/query")
  public Response queryOptions() {
    return noContent();
  }

  @POST
  @Path("/query")
  @Operation(summary = "used by panels to get data",
          tags = {"query"},
          responses = {
                  @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                  @ApiResponse(responseCode = "405", description = "Invalid input")
          })
  public String query(@Parameter(description = "query sent from the dashboard" , required = true) Query query ) {

    ArrayList<TimeSeriesResult> results = new ArrayList<>();
    if (query.getTargets() != null) {
      Target[] target = query.getTargets();
      for (Target target1 : target) {

        TimeSeriesResult timeSeriesResult = new TimeSeriesResult();
        timeSeriesResult.setTarget(target1.getTarget());
        timeSeriesResult.setDatapoints(createDatapoints(query.getRange(), query.getMaxDataPoints()));
        results.add(timeSeriesResult);
      }
    }

    // moxy doesnt support multi-dimensional arrays so drop back to a json-string and rely on json response type
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=389815
    return results.toString();
  }

  /**
   * {
   *     "target":"upper_75",
   *     "datapoints":[
   *       [622, 1450754160000],
   *       [365, 1450754220000]
   *     ]
   *   },
   */

  private long[][] createDatapoints(Range range, int samples) {

    long[][] datapoints = new long[samples][0];

    long step = (range.getDuration()) / samples;

    for (int i = 0; i < samples; i++) {
      datapoints[i] = new long[] {(long) (Math.random() * (10 * i)), range.getStart() + i * step };
    }
    return datapoints;
  }
}
