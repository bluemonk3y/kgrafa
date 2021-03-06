# KGrafana - Kafka Grafana Datasource for timeseries metrics


Use Kafka as a Grafana Datasource by running Streams Jobs against topics and building the timeseries metrics for charting in Grafana

See: 
- http://docs.grafana.org/plugins/developing/datasources/

- https://github.com/grafana/grafana/blob/master/docs/sources/plugins/developing/datasources.md

## Running Grafana on OSX
- cd /usr/local/Cellar/grafana/4.4.1_1/share/grafana/
- grafana-server start

# Plugin location
/usr/local/var/lib/grafana/plugins
/usr/local/Cellar/grafana/4.4.1_1/share/grafana/public/app/plugins/datasource


# Grafana log file location
/usr/local/Cellar/grafana/4.4.1_1/share/grafana/data/log

 ## User interface and Endpoints
  - REST: http://localhost:8080/metrics 
  - OPEN-API-SPEC: http://localhost:8080/openapi.json
  - SWAGGER: http://localhost:8080/swagger/index.html 
 
 

# Configure the Datasource with Provisioning
It’s now possible to configure datasources using config files with Grafana’s provisioning system. You can read more about how it works and all the settings you can set for datasources on the provisioning docs page

Here are some provisioning examples for this datasource.

apiVersion: 1

datasources:
  - name: KGrafa
    type: kgrafa
    access: proxy
    url: http://localhost:9090
    jsonData:
      dataResolution: 1
      grafaVersion: 1