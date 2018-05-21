package org.artifactory.storage.db.event.service;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import org.artifactory.common.ConstantValues;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Class that collects metrics of node events.
 *
 * @author Yossi Shaul
 */
@Component
public class EventsMetrics {

    @Autowired
    private MetricRegistry metricsRegistry;

    @Autowired
    private MBeanRegistrationService mbeansService;

    private boolean enabled = false;
    private Meter events;   // meter for all events

    /**
     * Map of meters by type
     */
    private ConcurrentMap<EventType, Meter> eventsByType = Maps.newConcurrentMap();

    @PostConstruct
    public void init() {
        enabled = ConstantValues.nodeEventsEnabled.getBoolean() && ConstantValues.nodeEventsMetricsEnabled.getBoolean();
        if (!enabled) {
            return;
        }
        events = metricsRegistry.meter(metricsName("ALL"));

        // create meter per event type
        Arrays.stream(EventType.values()).
                forEach(et -> eventsByType.put(et, metricsRegistry.meter(metricsName(et.name()))));

        JmxReporter.forRegistry(metricsRegistry)
                .createsObjectNamesWith((type, domain, name) ->
                        mbeansService.createObjectName("Metrics, name=Event", name))
                .filter((name, metric) -> name.startsWith("events")).build()
                .start();
    }

    private String metricsName(String name) {
        return name("events", name);
    }

    public void event(EventInfo event) {
        if (enabled) {
            events.mark();
            eventsByType.get(event.getType()).mark();
        }
    }

}
