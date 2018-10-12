package io.confluent.kgrafa.model.metric;

public class MetricStats {

  private int total;
  private double sum;
  private double min = Double.MAX_VALUE;
  private double max = Double.MIN_VALUE;
  private long time;

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
    total++;
    sum += metric.getValue();
    min = Math.min(min, metric.getValue());
    max = Math.max(max, metric.getValue());
    return this;

  }

  @Override
  public String toString() {
    return "MetricStats{" +
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


  static public final class TaskStatsSerde extends WrapperSerde<MetricStats> {
    public TaskStatsSerde() {
      super(new JsonSerializer<>(), new JsonDeserializer<>(MetricStats.class));
    }
  }
}
