package io.confluent.kgrafa.model;

/**
 * { "refId": "B", "target": "upper_75" },
 */
public class Target {
    String refId = "B";
    String target = "/biz/one/two resource metricname";

    public Target() {
    }

    public Target(String refId, String target) {
        this.refId = refId;
        this.target = target;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getRefId() {
        return refId;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "Target{" +
                "refId='" + refId + '\'' +
                ", target='" + target + '\'' +
                '}';
    }

    public String getTargetTopic() {
        return target.split(" ")[0];
    }
}
