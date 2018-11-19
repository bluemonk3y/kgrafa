package io.confluent.kgrafa.utils;

import io.confluent.kgrafa.model.metric.Metric;

import java.util.Map;
import java.util.TreeMap;

public class MetricDataProvider {
    public static final Map<String, Metric> data = new MetricDataProvider().buildData();

    private Map<String, Metric> buildData() {
        Map<String, Metric> dataMap = new TreeMap<>();
        String prefix = "prefix";
        String[] sources = {"source-1", "source-2"};
        String[] resources = {"res-1", "res-2"};

        dataMap.put("0", new Metric(prefix, sources[0], resources[0], "cpu", 0, 1000));
        dataMap.put("1", new Metric(prefix, sources[1], resources[1], "cpu", 1, 2000));
        dataMap.put("2", new Metric(prefix, sources[0], resources[0], "cpu", 2, 3000));
        dataMap.put("3", new Metric(prefix, sources[1], resources[1], "cpu", 3, 4000));
        dataMap.put("4", new Metric(prefix, sources[0], resources[0], "cpu", 4, 5000));
        dataMap.put("5", new Metric(prefix, sources[1], resources[1], "cpu", 5, 6000));
        return dataMap;
    }

}
