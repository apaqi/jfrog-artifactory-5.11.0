/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.db.event.dao;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.event.entity.EventRecord;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.event.EventType;
import org.jfrog.storage.util.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A data access object for the events table.
 *
 * @author Yossi Shaul
 */
@Repository
public class EventsDao extends BaseDao {

    @Autowired
    public EventsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public void create(EventRecord e) throws SQLException {
        jdbcHelper.executeUpdate("INSERT INTO node_events " +
                        "(event_id, timestamp, event_type, path) VALUES(?, ?, ?, ?)",
                e.getEventId(), e.getTimestamp(), e.getType().code(), e.getPath());
    }

    /**
     * Loads all the events newer from the given timestamp
     *
     * @param timestamp Timestamp in millis
     * @return Events newer than the given timestamp ordered by timestamp
     */
    @Nonnull
    public List<EventRecord> loadNewer(long timestamp, String repoKey) throws SQLException {
        return loadEventsSince(timestamp, repoKey, false);
    }

    /**
     * Loads all the events newer or same as the given timestamp
     *
     * @param timestamp Timestamp in millis
     * @return Events newer than the given timestamp ordered by timestamp
     */
    public List<EventRecord> loadNewerOrSame(long timestamp, String repoKey) throws SQLException {
        return loadEventsSince(timestamp, repoKey, true);
    }

    @Nonnull
    private List<EventRecord> loadEventsSince(long timestamp, String repoKey, boolean inclusive) throws SQLException {
        ResultSet rs = null;
        try {
            String comparator = inclusive ? ">=" : ">"; // whether to include or not events with the same timestamp
            List<EventRecord> entries = Lists.newArrayList();
            rs = jdbcHelper.executeSelect("SELECT * FROM node_events " +
                    "WHERE timestamp " + comparator + " ? " +
                    "AND path like ? " +
                    "ORDER BY timestamp ASC, event_id ASC", timestamp, repoKey + "/%");
            while (rs.next()) {
                entries.add(eventFromResultSet(rs));
            }
            return entries;
        } finally {
            DbUtils.close(rs);
        }
    }

    public long getEventsCount() throws SQLException {
        return jdbcHelper.executeSelectCount("SELECT COUNT(*) FROM node_events");
    }

    public List<EventRecord> loadAll() throws SQLException {
        ResultSet rs = null;
        try {
            List<EventRecord> entries = Lists.newArrayList();
            rs = jdbcHelper.executeSelect("SELECT * FROM node_events ORDER BY timestamp ASC, event_id ASC");
            while (rs.next()) {
                entries.add(eventFromResultSet(rs));
            }
            return entries;
        } finally {
            DbUtils.close(rs);
        }
    }

    /**
     * Delete all events from the event log. Internal use only!
     */
    public void deleteAll() throws SQLException {
        jdbcHelper.executeUpdate("DELETE FROM node_events");
    }

    private EventRecord eventFromResultSet(ResultSet rs) throws SQLException {
        return new EventRecord(rs.getLong("event_id"), rs.getLong("timestamp"),
                EventType.fromCode(rs.getShort("event_type")), rs.getString("path"));
    }
}
