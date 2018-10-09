package io.confluent.kgrafa.model;

/**
 *  "annotation": {
 *    *     "datasource": "generic datasource",
 *    *     "enable": true,
 *    *     "name": "annotation name"
 *    *   }
 */
public class Annnotation {
  String datasource = "generic datasource";
  boolean enable = true;
  String name = "annotation name";

  public Annnotation(){
  }
  public String getDatasource() {
    return datasource;
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
