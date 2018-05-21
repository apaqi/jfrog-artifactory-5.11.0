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

package org.artifactory.storage.db.event.service;

import org.artifactory.common.ConstantValues;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.event.dao.EventsDao;
import org.artifactory.storage.db.event.entity.EventRecord;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A business service to interact with the events table.
 *
 * @author Yossi Shaul
 */
@Service
public class EventsServiceImpl implements EventsService, InternalEventsService {
    private static final Logger log = LoggerFactory.getLogger(EventsServiceImpl.class);
    /**
     * Optional file logger to print events to a file. Format of the file is timestamp|type|path
     */
    private static final Logger eventLog = LoggerFactory.getLogger("events");

    @Autowired
    private InternalDbService dbService;

    @Autowired
    private EventsDao eventsDao;

    @Autowired
    private EventsMetrics metrics;

    @Override
    public boolean isEnabled() {
        return ConstantValues.nodeEventsEnabled.getBoolean();
    }

    @Override
    public long getEventsCount() {
        try {
            return eventsDao.getEventsCount();
        } catch (SQLException e) {
            throw new StorageException("Failed to count events", e);
        }
    }

    @Override
    public void appendEvents(List<EventInfo> events) {
        for (EventInfo event : events) {
            log.trace("Appending event {}", event);
            long eventId = dbService.nextId();
            try {
                eventsDao.create(toEventRecord(event, eventId));
                metrics.event(event);
            } catch (SQLException e) {
                throw new StorageException("Failed to persist event", e);
            }
        }
        logEvents(events);
    }

    @Override
    public List<EventInfo> getEventsAfterInclusive(long timestamp, String repoKey) {
        try {
            List<EventRecord> records = eventsDao.loadNewerOrSame(timestamp, repoKey);
            return records.stream().map(this::toEventInfo).collect(Collectors.toList());
        } catch (SQLException e) {
            throw new StorageException("Failed to persist event", e);
        }
    }

    @Override
    public List<EventInfo> getEventsAfter(long timestamp, String repoKey) {
        try {
            List<EventRecord> records = eventsDao.loadNewer(timestamp, repoKey);
            return records.stream().map(this::toEventInfo).collect(Collectors.toList());
        } catch (SQLException e) {
            throw new StorageException("Failed to persist event", e);
        }
    }

    private EventInfo toEventInfo(EventRecord er) {
        return new EventInfo(er.getTimestamp(), er.getType(), er.getPath());
    }

    private EventRecord toEventRecord(EventInfo e, long id) {
        return new EventRecord(id, e.getTimestamp(), e.getType(), e.getPath());
    }

    @Override
    public List<EventInfo> getAllEvents() {
        try {
            List<EventRecord> records = eventsDao.loadAll();
            return records.stream().map(this::toEventInfo).collect(Collectors.toList());
        } catch (SQLException e) {
            throw new StorageException("Failed to persist event", e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            eventsDao.deleteAll();
        } catch (SQLException e) {
            throw new StorageException("Failed to persist event", e);
        }
    }

    private void logEvents(List<EventInfo> events) {
        if (eventLog.isInfoEnabled()) {
            events.forEach(e -> eventLog.info("{}|{}|{}", e.getTimestamp(), e.getType(), e.getPath()));
        }
        if (log.isDebugEnabled()) {
            events.forEach(e -> log.debug("{}|{}|{}", e.getTimestamp(), e.getType(), e.getPath()));
        }
    }
}
