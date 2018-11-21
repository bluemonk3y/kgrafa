package io.confluent.kgrafa.model.metric;

public class MetricStats {

    private String name;
    private int total;
    private double sum;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private long time;

    public MetricStats() {
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public MetricStats add(Metric metric) {
        this.name = metric.getName();
        total++;
        sum += metric.getValue();
        min = Math.min(min, metric.getValue());
        max = Math.max(max, metric.getValue());
        return this;

    }

    @Override
    public String toString() {
        return "MetricStats{" +
                "name='" + name + '\'' +
                ", total=" + total +
                ", sum=" + sum +
                ", min=" + min +
                ", max=" + max +
                ", time=" + time +
                '}';
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetricStats set(String name, long time) {
        setName(name);
        setTime(time);
        return this;
    }

    static public final class MetricStatsSerde extends WrapperSerde<MetricStats> {
        public MetricStatsSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>(MetricStats.class));
        }
    }
}
