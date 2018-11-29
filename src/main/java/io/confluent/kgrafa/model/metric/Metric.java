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

import java.util.Arrays;

/**
 * The Metric template for writing metric data to the kafka metrics topic in json format
 * this data is used by stream processors for generating percentile metrics
 * * /[tags]/[host]/[app-context]/[resource-name][metric-name]  value timestamp
 * i.e.
 * TAGS/myhost/SERVER_RESOURCES/cpu-2/cpu-idle 98.61 1329168255
 * /myhost/cpu-2/cpu-nice 0 1329168255
 * /myhost/cpu-2/cpu-user 0.8000 1329168255
 * /myhost/cpu-2/cpu-wait 0.8000 1329168255
 * /myhost/cpu-2/cpu-wait 0.8000 1329168255
 * <p>
 * TAGS:                             APP:          Resource                                                            Metric:value
 * metrics/dev-1//NAvery.local [ kafka.network {type=RequestMetrics, name=RequestQueueTimeMs, request=TxnOffsetCommit} [] 95thPercentile: 0.0 ]
 * <p>
 * metrics/dev-1//NAvery.local [ kafka.network {type=RequestMetrics, name=RemoteTimeMs, request=CreatePartitions} [] 95thPercentile: 0.0 ]
 * metrics/dev-1//NAvery.local [ kafka.network {type=RequestMetrics, name=RemoteTimeMs, request=CreatePartitions} [] 98thPercentile: 0.0 ]
 * metrics/dev-1//NAvery.local [ kafka.network {type=RequestMetrics, name=RemoteTimeMs, request=CreatePartitions} [] 99thPercentile: 0.0 ]
 */
public class Metric {

    // path ==  biz-label / env-label, host, app-label
    transient private String path;

    // CPU, network, App-JMX
    private String resource;

    // min, max, percentile
    private String name;

    private double value;

    public Metric() {
    }

    public Metric(String path, String resource, String name, double value, long time) {
        this.path = path;
        this.resource = resource;
        this.name = name;
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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }


    /**
     * Restrict setters to avoid json serialization
     *
     * @return
     */
    public String path() {
        return path;
    }

    public void path(String bizTag, String envTag, String host, String appId) {
        this.path = String.format("%s/%s/%s/%s", bizTag, envTag, host, appId);
    }

    public void path(String path) {
        this.path = path;
    }


    public String canonicalName() {
        return path + " " + resource + " " + name;
    }

    static public String getPathAsTopic(String path) {
        if (path.contains(" ")) path = path.split(" ")[0];
        return path.replace("/", "_SLSH_");
    }

    static public String getTopicAsPath(String path) {
        return path.replace("_SLSH_", "/");
    }

    public static boolean isPathMatch(String[] metricTopicPath, String[] queryParts) {
        if (queryParts.length == 3) return _isPathMatch(metricTopicPath, queryParts);
        if (queryParts.length == 2)
            return _isPathMatch(metricTopicPath, new String[]{queryParts[0], queryParts[1], "*"});
        if (queryParts.length == 1) return _isPathMatch(metricTopicPath, new String[]{queryParts[0], "*", "*"});
        return false;
    }

    static public boolean _isPathMatch(String[] canonicalMetric, String[] filter) {
        int match = 0;
        if (matches(filter, canonicalMetric, 0)) match++;
        if (matches(filter, canonicalMetric, 1)) match++;
        if (matches(filter, canonicalMetric, 2)) match++;
        return match == 3;
    }

    private static boolean matches(String[] filter, String[] canonicalMetric, int index) {
        boolean isSimpleMatch = filter[index].equals("*") || canonicalMetric[index].contains(filter[index]);
        if (!isSimpleMatch && filter[index].contains("*")) {
            // handle partial match with filter broken by *. i.e. user*stuff matches <anything>user<anything>stuff<anything>
            String[] split = filter[index].split("\\*");
            long matchCount = Arrays.stream(split).filter(splitPart -> canonicalMetric[index].contains(splitPart)).count();
            isSimpleMatch = matchCount == split.length;

        }
        return isSimpleMatch;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "path='" + path + '\'' +
                ", resource='" + resource + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                ", time=" + time +
                '}';
    }

    /**
     * Key is on a per-topic basis so we can exclude topic level canonical path information /biz/team/host-name/app-id  leaving the key as resource:CPU metricname:load
     *
     * @return
     */
    public String getKey() {
        return resource + name;
    }
}
