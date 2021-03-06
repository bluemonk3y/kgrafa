package io.confluent.kgrafa.util;


import java.util.Collections;
import java.util.List;

public class ConsumerState {
    private final String topic;
    private final List<PartitionOffset> offsets;

    /**
     * Constructor.
     */
    public ConsumerState(final String topic, final List<PartitionOffset> offsets) {
        this.topic = topic;
        this.offsets = Collections.unmodifiableList(offsets);
    }

    public String getTopic() {
        return topic;
    }

    public List<PartitionOffset> getOffsets() {
        return offsets;
    }

    @Override
    public String toString() {
        return "ConsumerState{"
                + "topic='" + topic + '\''
                + ", offsets=" + offsets
                + '}';
    }
}
