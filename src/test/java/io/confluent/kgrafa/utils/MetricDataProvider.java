package io.confluent.kgrafa.utils;

import io.confluent.kgrafa.model.metric.Metric;

import java.util.Map;
import java.util.TreeMap;

public class MetricDataProvider {

    public static Map<String, Metric> data(int timeLimit) {
        Map<String, Metric> dataMap = new TreeMap<>();
        String prefix = "prefix";
        String[] sources = {"source-1", "source-2"};
        String[] resources = {"res-1", "res-2"};
        int pos = 0;
        for (String s : sources) {
            for (String r : resources) {
                for (int t = 1; t <= timeLimit; t++) {
                    dataMap.put("" + pos, new Metric(prefix, s, r, "cpu", pos, t * 1000));
                    pos++;
                }
            }
        }

        return dataMap;
    }

}
