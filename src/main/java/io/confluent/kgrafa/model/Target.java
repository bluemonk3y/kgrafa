package io.confluent.kgrafa.model;

/**
  { "refId": "B", "target": "upper_75" },
 */
public class Target {
  String refId = "B";
  String target = "upper_75";

  public Target(){
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
}
