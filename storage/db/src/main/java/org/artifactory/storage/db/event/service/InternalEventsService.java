package org.artifactory.storage.db.event.service;

import org.artifactory.storage.event.EventInfo;

import java.util.List;

/**
 * Internal interface for the events service. Contains internal, experimental and test only methods. Use with caution!
 *
 * @author Yossi Shaul
 */
public interface InternalEventsService {
    /**
     * @return Ordered list of all the events. This might be a huge list. DO NOT USE IN PRODUCTION
     */
    List<EventInfo> getAllEvents();

    /**
     * Deletes all events from the event log
     */
    void deleteAll();

}
