package io.confluent.kgrafa.model.search;

/**
 * { "target": "upper_75" },
 */
public class Target {
    String target = "upper_75";

    public Target() {
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public boolean matches(String metricQuery) {
        // TODO: compile the pattern
        String[] s = metricQuery.split(" ");

        return false;
    }
}
