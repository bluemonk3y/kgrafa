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
package io.confluent.kgrafa.model;

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

  private String target = "upper_75";
  private long[][] datapoints;

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
