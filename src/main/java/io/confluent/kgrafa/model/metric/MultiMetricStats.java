package io.confluent.kgrafa.model.metric;

import java.util.HashMap;
import java.util.Map;

public class MultiMetricStats {

    public Map<String, MetricStats> stats = new HashMap<>();
    private String name;
    private long time;

    public MultiMetricStats() {
    }

    public MultiMetricStats addIt(Metric metric) {
        MetricStats metricStats = stats.computeIfAbsent(metric.canonicalName(), k -> new MetricStats());
        metricStats.add(metric);
        return this;
    }

    public MultiMetricStats set(String name, long time) {
        this.name = name;
        this.time = time;
        return this;
    }

    @Override
    public String toString() {
        return this.hashCode() + " " + super.toString();
    }
}
