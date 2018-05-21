package org.artifactory.storage.db.event.entity;

import lombok.Value;
import org.artifactory.storage.event.EventType;

/**
 * Represents a record in the events table.
 *
 * @author Yossi Shaul
 */
@Value
public class EventRecord implements Comparable<EventRecord> {
    private long eventId;
    private final long timestamp;
    private final EventType type;
    /**
     * Path of the item that received the event. Paths end with '/' represents a directory
     */
    private final String path;

    /**
     * Compare two events by timestamp and event id. Event id is used only if the timestamp is equal
     */
    @Override
    public int compareTo(EventRecord o) {
        if (this.getTimestamp() != o.getTimestamp()) {
            return Long.compare(this.getTimestamp(), o.getTimestamp());
        }
        return Long.compare(this.getEventId(), o.getEventId());
    }
}
