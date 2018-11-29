package io.confluent.kgrafa.util;

import io.prometheus.jmx.JmxScraper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class JmxScraperRestWriter implements JmxScraper.MBeanReceiver {

    private final Client client;
    private final WebTarget target;
    private String hostName;

    private String template = "{\n" +
            "  \"bizTag\": \"%s\",\n" +
            "  \"envTag\": \"%s\",\n" +
            "  \"host\": \"%s\",\n" +
            "  \"appId\": \"%s\",\n" +
            "  \"metric\": {\n" +
            "    \"resource\": \"%s\",\n" +
            "    \"name\": \"%s\",\n" +
            "    \"value\": %f,\n" +
            "    \"time\": %d\n" +
            "  }\n" +
            "}";
    private final String bizTag;
    private final String envTag;

    public JmxScraperRestWriter(String restEndpoint, String bizTag, String envTag) {

        this.bizTag = bizTag;
        this.envTag = envTag;

        client = ClientBuilder.newClient();

        target = client.target(restEndpoint).path("/kgrafa/putMetric");

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

        String metricJson = String.format(template, bizTag, envTag, hostName, domain.replace(" ", ""), getLabelFromList(attrKeys), getLabel(attrName, beanProperties), getNumeric(value), System.currentTimeMillis());

        target.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(metricJson, MediaType.APPLICATION_JSON), String.class);

    }

    private String getLabelFromList(List<String> attrKeys) {
        String collect = attrKeys.stream().collect(Collectors.joining("-"));
        if (collect.length() == 0) collect = "-";
        return collect;
    }


    private String getLabel(String attrName, LinkedHashMap<String, String> beanProperties) {
        //String collect = beanProperties.entrySet().stream().map(entry -> entry.getKey().replace(" ", "") + "-" + entry.getValue().replace(" ", "")).collect(Collectors.joining("."));
        String collect = beanProperties.values().stream().map(entry -> entry.replace(" ", "")).collect(Collectors.joining("."));
        String beanProps = collect.replace("\"", "'");
        beanProps += "." + attrName;
        return beanProps;
    }

    private double getNumeric(Object value) {
        Double v;
        if (value instanceof Long) {
            v = ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            v = (double) value;
        } else if (value instanceof Integer) {
            v = ((Integer) value).doubleValue();
        } else if (value instanceof Boolean) {
            v = ((Boolean) value).booleanValue() == true ? 1.0 : 0;
        } else {
            v = 0.0;
        }
        if (v.isNaN() || v.isInfinite()) v = 0.0;
        return v;
    }
}
