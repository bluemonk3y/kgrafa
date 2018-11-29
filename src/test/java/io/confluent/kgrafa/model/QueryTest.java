package io.confluent.kgrafa.model;

import io.confluent.kgrafa.model.metric.Metric;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class QueryTest {

    @Test
    public void passesFilter() {

        Query query = new Query();
        query.setTargets(new Target[]{new Target("refId", "biz-1/production/server-863_lx/apollo cpu idle")});

        Metric cpu = new Metric("biz-1/production/server-863_lx/apollo", "cpu", "idle   ", 1, 1);
        assertThat(query.passesFilter(cpu), is(true));


        Metric latency = new Metric("biz-1/production/server-863_lx/apollo", "latency", "min", 1, 1);
        assertThat(query.passesFilter(latency), is(false));
    }

    @Test
    public void passesWildcardFilter() {

        Query query = new Query();
        query.setTargets(new Target[]{new Target("refId", "biz-1/production/server-863_lx/apollo * idle")});

        Metric cpu = new Metric("biz-1/production/server-863_lx/apollo", "cpu", "idle   ", 1, 1);
        assertThat(query.passesFilter(cpu), is(true));


        Metric latency = new Metric("biz-1/production/server-863_lx/apollo", "latency", "idle", 1, 1);
        assertThat(query.passesFilter(latency), is(true));

        Metric notIdle = new Metric("biz-1/production/server-863_lx/apollo", "latency", "not", 1, 1);
        assertThat(query.passesFilter(notIdle), is(false));

    }

    @Test
    public void metricMatchesQuery() throws Exception {
        String[] q1 = "a/b/c/d someResource someMetric".split(" ");

        assertThat(Metric.isPathMatch(q1, "* someResource *".split(" ")), is(true));
        assertThat(Metric.isPathMatch(q1, "* someResource".split(" ")), is(true));
        assertThat(Metric.isPathMatch(q1, "*".split(" ")), is(true));
    }


    @Test
    public void metricTestPathToTopc() throws Exception {
        String path = "a/b/c/d";
        String pathAsTopic = Metric.getPathAsTopic(path);
        System.out.println(pathAsTopic);

        String topicAsPath = Metric.getTopicAsPath(pathAsTopic);
        assertEquals(topicAsPath, path);
        assertThat(pathAsTopic, not(containsString("/")));
    }

}