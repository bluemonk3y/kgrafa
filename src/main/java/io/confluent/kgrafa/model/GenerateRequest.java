package io.confluent.kgrafa.model;

public class GenerateRequest {
    private String source;
    private String resource;
    private String metric;

    public int getCount() {
        return count;
    }

    private final int count;

    public GenerateRequest(String source, String resource, String metric, int count) {
        this.source = source;
        this.resource = resource;
        this.metric = metric;
        this.count = count;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

}
