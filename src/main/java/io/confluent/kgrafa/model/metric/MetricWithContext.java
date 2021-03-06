package io.confluent.kgrafa.model.metric;

public class MetricWithContext {
    String bizTag;
    String envTag;
    String host;
    String appId;
    long time;
    Metric metric;

    public MetricWithContext() {
    }

    public MetricWithContext(String bizTag, String envTag, String host, String appId, long time, Metric metric) {
        this.time = time;
        this.bizTag = bizTag;
        this.envTag = envTag;
        this.host = host;
        this.appId = appId;
        this.metric = metric;
    }

    public String getBizTag() {
        return bizTag;
    }

    public void setBizTag(String bizTag) {
        this.bizTag = bizTag;
    }

    public String getEnvTag() {
        return envTag;
    }

    public void setEnvTag(String envTag) {
        this.envTag = envTag;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Metric getMetric() {
        metric.time(this.time);
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
