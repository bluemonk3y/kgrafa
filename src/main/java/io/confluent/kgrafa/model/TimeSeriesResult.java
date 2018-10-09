package io.confluent.kgrafa.model;

import java.util.Arrays;

/**
 * [
 *   {
 *     "target":"upper_75",
 *     "datapoints":[
 *       [622, 1450754160000],
 *       [365, 1450754220000]
 *     ]
 *   },
 *   {
 *     "target":"upper_90",
 *     "datapoints":[
 *       [861, 1450754160000],
 *       [767, 1450754220000]
 *     ]
 *   }
 * ]
 */
public class TimeSeriesResult {

  String target = "upper_75";
  long[][] datapoints;

  public TimeSeriesResult(){

  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getTarget() {
    return target;
  }

  public void setDatapoints(long[][] datapoints) {
    this.datapoints = datapoints;
  }

  public long[][] getDatapoints() {
    return datapoints;
  }

  @Override
  public String toString() {
    return "\n{" +
            "\"target\":\"" + target + "\",\n" +
            "\"datapoints\": [" + stringifyArray(datapoints) +
            "]}";
  }

  private String stringifyArray(long[][] datapoints) {
    StringBuilder results = new StringBuilder();
    for (long[] datapoint : datapoints) {
      results.append("[").append(datapoint[0]).append(",").append(datapoint[1]).append("]").append(",");
    }

    String rr = results.toString();
    return rr.substring(0, rr.length()-1);
  }
}
