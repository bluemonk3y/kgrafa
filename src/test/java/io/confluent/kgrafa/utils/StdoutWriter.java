package io.confluent.kgrafa.utils;

import io.prometheus.jmx.JmxScraper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class StdoutWriter implements JmxScraper.MBeanReceiver {

    private String hostName;
    private List<String> tags;

    public StdoutWriter(String[] tags) {
        this.tags = new ArrayList<>(Arrays.asList(tags));
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "unknown";
        }
    }

    public void recordBean(
            String domain,
            LinkedHashMap<String, String> beanProperties,
            LinkedList<String> attrKeys,
            String attrName,
            String attrType,
            String attrDescription,
            Object value) {


        StringBuilder sb = new StringBuilder();
        tags.stream().forEach(tag -> sb.append(tag).append("/"));

        // REST: Path: [/2-tags/hostname ] Metric[ String app-context, String metric-name double value]
        //


        //global tag / 2 tags for heirarchy (team/env) / hostname / app-context (i.e.

        System.out.println(String.format("%s/%s [ %s %s %s %s: %s ]", sb.toString(), hostName, domain, beanProperties, attrKeys, attrName, value));
    }
}
