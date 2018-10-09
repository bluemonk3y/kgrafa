package io.confluent.kgrafa.model;


/**
 * [
 *   {
 *     "annotation": {
 *       "name": "annotation name", //should match the annotation name in grafana
 *       "enabled": true,
 *       "datasource": "generic datasource",
 *      },
 *     "title": "Cluster outage",
 *     "time": 1457075272576,
 *     "text": "Joe causes brain split",
 *     "tags": "joe, cluster, failure"
 *   }
 * ]
 */
public class AnnnotationResult {
  Annnotation annotation;
  String title = "Cluster outage";
  long time = System.currentTimeMillis();
  String text = "Joe causes brain split";
  String tags = "joe, cluster, failure";

  public Annnotation getAnnotation() {
    return annotation;
  }

  public void setAnnotation(Annnotation annotation) {
    this.annotation = annotation;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }
}
