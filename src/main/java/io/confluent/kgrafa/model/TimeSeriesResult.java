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

import io.confluent.kgrafa.model.metric.MetricStats;

import java.text.DecimalFormat;
import java.util.List;

/**
 * [
 * {
 * "target":"upper_75",
 * "datapoints":[
 * [622, 1450754160000],
 * [365, 1450754220000]
 * ]
 * },
 * {
 * "target":"upper_90",
 * "datapoints":[
 * [861, 1450754160000],
 * [767, 1450754220000]
 * ]
 * }
 * ]
 */
public class TimeSeriesResult {

    private String target = "upper_75";
    private double[][] datapoints;

    public TimeSeriesResult() {

    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }


    public double[][] getDatapoints() {
        return datapoints;
    }

    @Override
    public String toString() {
        return "\n{" +
                "\"target\":\"" + target + "\",\n" +
                "\"datapoints\": [" + stringifyArray(datapoints) +
                "]}";
    }

    private String stringifyArray(double[][] datapoints) {
        DecimalFormat decimalFormat = new DecimalFormat("0.###");
        StringBuilder results = new StringBuilder();
        for (double[] datapoint : datapoints) {
            results.append("[").append(decimalFormat.format(datapoint[0])).append(",").append(Double.valueOf(datapoint[1]).longValue()).append("]").append(",");
        }

        String rr = results.toString();
        return rr.substring(0, rr.length() - 1);
    }

    public void setValues(String name, List<MetricStats> metrics) {

        this.setTarget(name);
        double[][] datapoints = new double[metrics.size()][0];

        int i = 0;
        for (MetricStats metric : metrics) {
            datapoints[i] = new double[]{metric.getMax(), metric.getTime()};
            i++;
        }

        this.datapoints = datapoints;
    }
}
