package com.kislitsyn.test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.kislitsyn.test.metrics.PrometheusHazelcastMetricsExporter;
import com.kislitsyn.test.metrics.PrometheusMetricsServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by Anton Kislitsyn on 03/12/2016
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final String METRICS_PORT = "metrics.port";

    public static void main(String[] args) throws IOException {
        Properties props = System.getProperties();
        Map<String, String> properties = props.stringPropertyNames().stream()
                .collect(Collectors.toMap(name -> name, props::getProperty));

        log.info("logging level: {}", properties.get("logging.threshold"));
        Configurator.setRootLevel(Level.toLevel(properties.get("logging.threshold"), Level.TRACE));

        log.info("Server start");

        PrometheusMetricsServer metricsServer =
                new PrometheusMetricsServer(Integer.valueOf(properties.getOrDefault(METRICS_PORT, "8080")));

        AtomicReference<HazelcastInstance> instance = new AtomicReference<>();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (instance.get() != null) {
                    instance.get().shutdown();
                }
                try {
                    metricsServer.close();
                } catch (IOException e) {
                    log.error("shutdown failed", e);
                }
            }
        });

        new Thread() {
            @Override
            public void run() {
                HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
                log.info("server config: {}", hazelcastInstance.getConfig());
                instance.set(hazelcastInstance);
                PrometheusHazelcastMetricsExporter metricsExporter = new PrometheusHazelcastMetricsExporter(instance);
                metricsExporter.start();
            }
        }.start();

        log.info("Server stop");
    }
}
