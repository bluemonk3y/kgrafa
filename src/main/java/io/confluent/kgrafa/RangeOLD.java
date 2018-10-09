package io.confluent.kgrafa;


import javax.json.JsonObject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 *
 */
public class RangeOLD {

  private long start;
  private long end;
  private long duration;

  public RangeOLD(long start, long end) {

    this.start = start;
    this.end = end;
    this.duration = end - start;
  }

  public static RangeOLD from(JsonObject range) {

    if (range == null) {
      return new RangeOLD(System.currentTimeMillis(), System.currentTimeMillis());
    }

    String fromString = range.getString("from");
    String toString = range.getString("to");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    LocalDateTime fromDateTime = LocalDateTime.parse(fromString, formatter);
    LocalDateTime toDateTime = LocalDateTime.parse(toString, formatter);

    return new RangeOLD(fromDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            toDateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {

    return end;
  }

  public long getDuration() {

    return duration;
  }
}