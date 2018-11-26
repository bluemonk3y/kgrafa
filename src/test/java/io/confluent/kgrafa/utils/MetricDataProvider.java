package io.confluent.kgrafa.utils;

import io.confluent.kgrafa.model.metric.Metric;

import java.util.Map;
import java.util.TreeMap;

public class MetricDataProvider {

    public static Map<String, Metric> data(int timeIncrementMs, int timeMax) {
        Map<String, Metric> dataMap = new TreeMap<>();
        String[] sources = {"validator"};//, "sso", "enricher-segment"};
        String[] resources = {"cpu"};//, "throughput", "latency"};
        int pos = 0;
        for (String s : sources) {
            for (String r : resources) {
                int time = 0;
                while ((time += timeIncrementMs) < timeMax) {
//                for (int t = 1; t <= 10; t++) {
                    Metric metric = new Metric("", r, "avg", pos, time);
                    metric.path("biz", "production", "server_lnx", s);
                    dataMap.put("" + pos, metric);
                    pos++;
                }
            }
        }

        System.out.println("Test Data:" + dataMap);

        return dataMap;
    }

}
