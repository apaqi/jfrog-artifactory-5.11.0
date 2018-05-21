package org.artifactory.storage.db.event.itest.dao;

import org.artifactory.storage.db.event.dao.EventsDao;
import org.artifactory.storage.db.event.entity.EventRecord;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Units tests for {@link EventsDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class EventsDaoTest extends DbBaseTest {

    @Autowired
    private EventsDao dao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void countEvents() throws SQLException {
        assertEquals(dao.getEventsCount(), 18);
    }

    public void allEvents() throws SQLException {
        assertThat(dao.loadAll()).hasSize(18);
    }

    public void allEventsOrdered() throws SQLException {
        List<EventRecord> result = dao.loadAll();
        EventRecord[] ordered = result.stream().sorted().toArray(EventRecord[]::new);
        assertThat(result).hasSize(18).containsExactly((Object[]) ordered);
    }

    @Test(dependsOnMethods = {"countEvents", "allEvents", "allEventsOrdered"})
    public void createDirectoryNode() throws SQLException {
        EventRecord e = new EventRecord(50, System.currentTimeMillis(), EventType.create, "repo5/land/");
        dao.create(e);
    }

    public void loadNewerEventsAll() throws SQLException {
        List<EventRecord> result = dao.loadNewer(0L, "repo1");
        assertThat(result).hasSize(11);
        result.forEach(e -> {
            assertTrue(e.getPath().startsWith("repo1/"));
            assertTrue(e.getEventId() > 0);
            assertTrue(e.getTimestamp() > 0);
        });
    }

    // tests that the results from the database are ordered according to the event timestamp and id
    public void loadNewerEventsOrder() throws SQLException {
        List<EventRecord> result = dao.loadNewer(67676732L, "repo1");
        EventRecord[] ordered = result.stream().sorted().toArray(EventRecord[]::new);
        assertThat(result).hasSize(11).containsExactly((Object[]) ordered);
    }

    public void loadNewerEventsIsExclusive() throws SQLException {
        List<EventRecord> result = dao.loadNewer(1515653250003L, "repo2");
        assertThat(result).hasSize(2);
        result.forEach(e -> assertTrue(e.getTimestamp() > 1515653250003L));
    }

    public void loadNewerOrSameEventsIsInclusive() throws SQLException {
        List<EventRecord> result = dao.loadNewerOrSame(1515653250003L, "repo2");
        assertThat(result).hasSize(3);
        result.forEach(e -> assertTrue(e.getTimestamp() >= 1515653250003L));
    }

    public void loadNewerEventsNoEvents() throws SQLException {
        assertThat(dao.loadNewer(1515653255555L, "repo2")).hasSize(0);
    }

}