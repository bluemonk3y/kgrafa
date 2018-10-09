package io.confluent.kgrafa.model;

/**
 {
 "range": { "from": "2015-12-22T03:06:13.851Z", "to": "2015-12-22T06:48:24.137Z" },
 "interval": "5s",
 "targets": [
 { "refId": "B", "target": "upper_75" },
 { "refId": "A", "target": "upper_90" }
 ],
 "format": "json",
 "maxDataPoints": 2495 //decided by the panel
 }
 */
public class Query {
  Range range = new Range();
  String interval = "5s";
  Target[] targets;
  String format = "json";
  int maxDataPoints = 60;

  public Query(){

  }

  public Range getRange() {
    return range;
  }

  public void setRange(Range range) {
    this.range = range;
  }

  public String getInterval() {
    return interval;
  }

  public void setInterval(String interval) {
    this.interval = interval;
  }

  public Target[] getTargets() {
    return targets;
  }

  public void setTargets(Target[] targets) {
    this.targets = targets;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public int getMaxDataPoints() {
    return maxDataPoints;
  }

  public void setMaxDataPoints(int maxDataPoints) {
    this.maxDataPoints = maxDataPoints;
  }
}
