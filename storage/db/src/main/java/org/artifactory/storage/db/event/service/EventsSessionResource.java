package org.artifactory.storage.db.event.service;

import com.google.common.collect.Lists;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventType;
import org.artifactory.storage.event.EventsService;
import org.artifactory.storage.tx.SessionResource;
import org.artifactory.storage.tx.SessionResourceAdapter;
import org.artifactory.util.RepoPathUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The events session resource is a {@link SessionResource} that receives low level storage events and stores them in
 * the events table once the transaction is committed.
 * Like any other session resource, it is attached to a single thread, lives and dies in the context of the current
 * database transaction.
 *
 * @author Yossi Shaul
 */
public class EventsSessionResource extends SessionResourceAdapter {

    private List<EventInfo> events = Lists.newArrayList();

    @Override
    public void beforeCommit() {
        EventsService eventsService = ContextHelper.get().beanForType(EventsService.class);
        if (!eventsService.isEnabled()) {
            return;
        }
        if (events.isEmpty()) {
            return;
        }

        List<EventInfo> filteredEvents = events.stream()
                .filter(item -> !RepoPathUtils.isTrash(item.getRepoPath()))
                .collect(Collectors.toList());
        eventsService.appendEvents(filteredEvents);
    }

    public void itemCreated(VfsItem item) {
        events.add(new EventInfo(System.currentTimeMillis(), EventType.create, item.getRepoPath().toPath()));
    }

    public void itemUpdated(VfsItem item) {
        events.add(new EventInfo(System.currentTimeMillis(), EventType.update, item.getRepoPath().toPath()));
    }

    public void itemDeleted(VfsItem item) {
        events.add(new EventInfo(System.currentTimeMillis(), EventType.delete, item.getRepoPath().toPath()));
    }

    public void propertiesModified(VfsItem item) {
        events.add(new EventInfo(System.currentTimeMillis(), EventType.props, item.getRepoPath().toPath()));
    }

    @Override
    public void afterCompletion(boolean commit) {
        // events are persisted before the transaction is committed
    }
}
