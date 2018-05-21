package org.artifactory.storage.event;

import java.util.List;

/**
 * A business service interface to interact with the tree events.
 *
 * @author Yossi Shaul
 */
public interface EventsService {
    /**
     * @return True if node events recording is enabled
     */
    boolean isEnabled();

    /**
     * @return Count of all events in the log
     */
    long getEventsCount();

    /**
     * List of ordered events to add
     *
     * @param events List of ordered events to create
     */
    void appendEvents(List<EventInfo> events);

    /**
     * @param timestamp The timestamp to get events after
     * @param repoKey   The repository key or events to get
     * @return Ordered list of events happened since the input timestamp (not inclusive) on the repo key
     */
    List<EventInfo> getEventsAfter(long timestamp, String repoKey);

    /**
     * @param timestamp The timestamp to get events since
     * @param repoKey   The repository key or events to get
     * @return Ordered list of events happened since the input timestamp on the repo key. Including events happened at
     * the same time as the input timestamp
     */
    List<EventInfo> getEventsAfterInclusive(long timestamp, String repoKey);
}
