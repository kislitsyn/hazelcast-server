package com.kislitsyn.test.metrics;

import com.hazelcast.monitor.LocalMapStats;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Anton Kislitsyn on 19/08/2017
 */
public class PrometheusHazelcastMapMetricsExporter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Gauge entryCount;
    private final Gauge entryMemoryCost;
    private final Gauge backupEntryCount;
    private final Gauge backupEntryMemoryCost;
    private final Gauge heapCost;

    private final String mapName;

    public PrometheusHazelcastMapMetricsExporter(String mapName) {
        this.mapName = mapName;
        log.info("create map metrics exporter: mapName={}", mapName);

        entryCount = register(Gauge.build("entry_count", "Count owned entries"));
        entryMemoryCost = register(Gauge.build("entry_memory_cost", "Memory cost of owned entries"));

        backupEntryCount = register(Gauge.build("backup_entry_count", "Count owned backup entries"));
        backupEntryMemoryCost = register(Gauge.build("backup_entry_memory_cost", "Memory cost of owned backup entries"));

        heapCost = register(Gauge.build("heap_cost", "Heap cost of map"));
    }

    private Gauge register(Gauge.Builder builder) {
        return builder
                .subsystem("map")
                .namespace("hazelcast")
                .labelNames("name")
                .register();
    }

    public void sendMetrics(LocalMapStats stats) {
        entryCount.labels(mapName).set(stats.getOwnedEntryCount());
        entryMemoryCost.labels(mapName).set(stats.getOwnedEntryMemoryCost());
        backupEntryCount.labels(mapName).set(stats.getBackupEntryCount());
        backupEntryMemoryCost.labels(mapName).set(stats.getBackupEntryMemoryCost());
        heapCost.labels(mapName).set(stats.getHeapCost());
    }

}
