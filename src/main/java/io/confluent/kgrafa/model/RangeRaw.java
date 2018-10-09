package io.confluent.kgrafa.model;

import java.util.Date;

/**
 * "range": { "from": "now-3h", "to": "now" },
 */
public class RangeRaw {

  String from = "now-3h";
  String now = "now";

  public RangeRaw(){
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getNow() {
    return now;
  }

  public void setNow(String now) {
    this.now = now;
  }
}
