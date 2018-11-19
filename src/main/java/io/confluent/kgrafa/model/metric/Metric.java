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
package io.confluent.kgrafa.model.metric;

import java.util.Date;

/**
 * The Metric template for writing metric data to the kafka metrics topic in json format
 * this data is used by stream processors for generating percentile metrics
 *  * /[metrics]/[SOURCE]/[resource]/[metric-name]  value timestamp
 * i.e.
 * /myhost/cpu-2/cpu-idle 98.61 1329168255
 * /myhost/cpu-2/cpu-nice 0 1329168255
 * /myhost/cpu-2/cpu-user 0.8000 1329168255
 * /myhost/cpu-2/cpu-wait 0.8000 1329168255
 * /myhost/cpu-2/cpu-wait 0.8000 1329168255
 *
 * note: the above is sample data from the 'Collectd Write Graphite plugin'
 */
public class Metric {
  transient private String name;
  private String resource;
  private double value;

  public Metric() {
  }

  public Metric(String prefix, String source, String resource, String metric, double value, long time) {
    if (prefix != null) {
      this.name = String.format("%s_%s_%s_%s", prefix, source, resource, metric, value);
    }
    this.resource = source;
    this.value = value;
    this.time = time;
  }

  private long time;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }


  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

    @Override
    public String toString() {
        return "Metric{" +
                "name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", value=" + value +
                ", time=" + time +
                ", timeS=" + new Date(time) +
                '}';
    }
}
