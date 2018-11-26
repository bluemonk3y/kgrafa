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

import io.confluent.kgrafa.model.metric.Metric;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
 */
public class Query {
    private Range range = new Range();
    private String interval = "5s";
    private Target[] targets;
    private String format = "json";
    private int maxDataPoints = 60;

    public Query() {

    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public Target[] getTargets() {
        return targets;
    }

    public void setTargets(Target[] targets) {
        this.targets = targets;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getMaxDataPoints() {
        return maxDataPoints;
    }

    public void setMaxDataPoints(int maxDataPoints) {
        this.maxDataPoints = maxDataPoints;
    }

    public long getIntervalAsMillis() {
        if (interval.endsWith("ms")) {
            return Long.parseLong(interval.substring(0, interval.length() - 2));
        }

        if (interval.endsWith("s")) {
            return Long.parseLong(interval.substring(0, interval.length() - 1)) * 1000;
        }
        if (interval.endsWith("m")) {
            return Long.parseLong(interval.substring(0, interval.length() - 1)) * 1000 * 60;
        }
        return Long.parseLong(interval);
    }

    @Override
    public String toString() {
        return "Query{" +
                "range=" + range +
                ", interval='" + interval + '\'' +
                ", targets=" + Arrays.toString(targets) +
                ", format='" + format + '\'' +
                ", maxDataPoints=" + maxDataPoints +
                '}';
    }

    public List<String> getTopicsFromTargets() {
        return Arrays.stream(targets).map(item -> item.getTargetTopic()).collect(Collectors.toList());
    }

    public boolean passesFilter(Metric metric) {
        boolean passesTimeFilter = metric.getTime() >= getRange().getStart() && metric.getTime() <= getRange().getEnd();
        if (!passesTimeFilter) return false;

        for (Target target : targets) {
            if (!metric.matchesFilter(target.getTarget())) return false;
        }

        return true;
    }
}
