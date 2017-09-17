package com.kislitsyn.test;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import io.prometheus.client.Counter;


/**
 * Created by Anton Kislitsyn on 03/12/2016
 */
public class EventListener implements EntryAddedListener<String, String>, EntryEvictedListener<String, String> {


    private static Counter addedCounter = Counter.build("items_added_total", "Total added items").register();
    private static Counter evictedCounter = Counter.build("items_evicted_total", "Total evicted items").register();

    @Override
    public void entryAdded(EntryEvent<String, String> event) {
        addedCounter.inc();
    }

    @Override
    public void entryEvicted(EntryEvent<String, String> event) {
        evictedCounter.inc();
    }

}
