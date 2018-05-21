package org.artifactory.storage.db.event.entity;

import org.artifactory.storage.event.EventType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link EventRecord}
 *
 * @author Yossi Shaul
 */
@Test
public class EventRecordTest {

    public void compareDifferentTimestamp() {
        EventRecord e1 = new EventRecord(1, 1, EventType.create, "/");
        EventRecord e2 = new EventRecord(2, 223, EventType.create, "/");
        assertEquals(e1.compareTo(e2), -1, "e1 is before e2");
    }

    public void compareSameTimestampDifferentId() {
        EventRecord e1 = new EventRecord(2, 223, EventType.create, "/");
        EventRecord e2 = new EventRecord(1, 223, EventType.delete, "/");
        assertEquals(e1.compareTo(e2), 1, "e1 is after e2 according to id");
    }

    public void compareSameTimestampSameId() {
        EventRecord e1 = new EventRecord(1, 2, EventType.create, "/");
        EventRecord e2 = new EventRecord(1, 2, EventType.delete, "/");
        assertEquals(e1.compareTo(e2), 0, "e1 is same as e2");
    }

}