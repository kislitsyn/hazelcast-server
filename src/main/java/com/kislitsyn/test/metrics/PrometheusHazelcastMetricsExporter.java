package com.kislitsyn.test.metrics;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Anton Kislitsyn on 19/08/2017
 */
public class PrometheusHazelcastMetricsExporter implements Closeable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AtomicReference<HazelcastInstance> hazelcastInstance;
    private final ScheduledExecutorService executorService;
    private final Map<String, PrometheusHazelcastMapMetricsExporter> registry;

    public PrometheusHazelcastMetricsExporter(AtomicReference<HazelcastInstance> hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        executorService = Executors.newSingleThreadScheduledExecutor();
        registry = new ConcurrentHashMap<>();
    }

    public void start() {
        log.info("start");
        Runnable sendMetrics = () -> hazelcastInstance.get().getConfig().getMapConfigs().values().stream()
                .map(MapConfig::getName)
                .map(mapName -> hazelcastInstance.get().getMap(mapName))
                .forEach(PrometheusHazelcastMetricsExporter.this::sendMapStats);
        executorService.scheduleAtFixedRate(sendMetrics, 0, 1, TimeUnit.SECONDS);
    }

    private void sendMapStats(IMap<?, ?> map) {
        try {
            registry.computeIfAbsent(map.getName(), PrometheusHazelcastMapMetricsExporter::new)
                    .sendMetrics(map.getLocalMapStats());
        } catch (Exception e) {
            log.warn("send metrics failed", e);
        }
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
    }
}
