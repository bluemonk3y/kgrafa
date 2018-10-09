package io.confluent.kgrafa.model;

import java.util.Date;

/**
 * "range": { "from": "2015-12-22T03:06:13.851Z", "to": "2015-12-22T06:48:24.137Z" },
 */
public class Range {

  Date from = new Date(System.currentTimeMillis() - 60 * 1000);
  Date to = new Date();

  public Range(){

  }

  public void setFrom(Date from) {
    this.from = from;
  }

  public Date getFrom() {
    return from;
  }

  public void setTo(Date to) {
    this.to = to;
  }

  public Date getTo() {
    return to;
  }

  public long getDuration() {
    return to.getTime() - from.getTime();
  }

  public long getStart() {
    return from.getTime();
  }
}
