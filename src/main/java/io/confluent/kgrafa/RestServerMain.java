/**
 * Copyright 2018 Confluent Inc.
 * <p>
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/AGPL-3.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kgrafa;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Properties;

public class RestServerMain {

    private static Logger log = LoggerFactory.getLogger(RestServerMain.class);

    private static Server server;

    static String resourcesFolder = System.getProperty("kgrafana.resources.folder", "./resources");
    private static String boostrapServers = System.getProperty("bootstrap.servers", "localhost:9092");
    private static int port = Integer.getInteger("kgrafa.rest.port", 8080);


    public static void main(String[] args) throws Exception {
        initialize();
        start();
        join();
        destroy();
    }

    public static void initialize() throws MalformedURLException {
        log.info("Initializing. \n Properties: \n\tkgrafa.rest.port = {}\n\tkgrafa.resources.folder = {}\n\tboostrap.servers = {}\n\t",
                port, resourcesFolder, boostrapServers);

        System.out.println("Path:" + new File(".").getAbsolutePath());

        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // http://localhost:8080/metrics
        ServletHolder apiServlet = context.addServlet(ServletContainer.class, "/*");

        apiServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, KGrafaResource.class.getCanonicalName());
        apiServlet.setInitParameter(ServerProperties.APPLICATION_NAME, KGrafaResource.class.getCanonicalName());

        apiServlet.setInitOrder(0);
        // configure swagger openapi path scanning
        apiServlet.setInitParameter(ServerProperties.PROVIDER_PACKAGES,
                "io.confluent.kwq,io.swagger.v3.jaxrs2.integration.resources");


        // http://localhost:8080/openapi.json
        ServletHolder swaggerHolder = new ServletHolder("swaggerResources", DefaultServlet.class);
        swaggerHolder.setInitParameter("dirAllowed", "true");
        swaggerHolder.setInitParameter("pathInfoOnly", "true");
        String resourcesDir = resourcesFolder + "/swagger";
        if (!new File(resourcesDir).exists()) {
            System.err.println("Resources folder doesnt exist, check:" + new File(resourcesDir).getAbsolutePath());
        }

        swaggerHolder.setInitParameter("resourceBase", resourcesDir);
        context.addServlet(swaggerHolder, "/swagger/*");


        // Lastly, set the default servlet for root content (always needed, to satisfy servlet spec)
        // nb: It is important that this is last.
        ServletHolder holderDef = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(holderDef, "/");


        registerLifecycleHandler(apiServlet);
    }

    private static void registerLifecycleHandler(ServletHolder apiServlet) {
        apiServlet.addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(LifeCycle lifeCycle) {
                try {
                    Properties properties = new Properties();
                    properties.put("bootstrap.servers", boostrapServers);
                    KGrafaInstance.getInstance(properties);
                } catch (Throwable t) {
                    log.error("Fatal error during startup", t);
                    t.printStackTrace();
                    System.exit(-1);
                }
            }

            @Override
            public void lifeCycleStarted(LifeCycle lifeCycle) {
            }

            @Override
            public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
            }

            @Override
            public void lifeCycleStopping(LifeCycle lifeCycle) {
            }

            @Override
            public void lifeCycleStopped(LifeCycle lifeCycle) {
            }
        });
    }

    public static void start() {
        try {
            server.start();
        } catch (Exception ex) {
            log.error("Failed to start", ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void destroy() {
        server.destroy();
    }

    public static void join() {
        try {
            server.join();
        } catch (Exception ex) {
            log.error("Failed to join", ex);
            System.exit(1);
        }
    }

    public static void stop() {
        try {
            server.stop();
        } catch (Exception ex) {
            log.error("Failed to stop", ex);
            ex.printStackTrace();
        }
    }
}
