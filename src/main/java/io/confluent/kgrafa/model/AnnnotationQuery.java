package io.confluent.kgrafa.model;

public class AnnnotationQuery {
  Range range;
  private RangeRaw rangeRaw;

  public void setRange(Range range) {
    this.range = range;
  }

  public void setRangeRaw(RangeRaw rangeRaw) {
    this.rangeRaw = rangeRaw;
  }

  public Annnotation getAnnotation() {
    return annotation;
  }

  public void setAnnotation(Annnotation annotation) {
    this.annotation = annotation;
  }

  private Annnotation annotation;

  public Range getRange() {
    return range;
  }

  public RangeRaw getRangeRaw() {
    return rangeRaw;
  }

  public Annnotation getAnnnotation() {
    return annotation;
  }
}
