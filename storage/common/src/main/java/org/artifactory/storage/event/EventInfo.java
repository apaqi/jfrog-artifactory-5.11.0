package org.artifactory.storage.event;

import lombok.Value;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;

/**
 * Represents a modification event on the artifacts tree.
 *
 * @author Yossi Shaul
 */
@Value
public class EventInfo {
    private final long timestamp;
    private final EventType type;
    private final String path;

    /**
     * @return The repo path for this event
     */
    public RepoPath getRepoPath() {
        return RepoPathFactory.create(path);
    }

    /**
     * @return True if this event is on a folder
     */
    public boolean isFolder() {
        return getRepoPath().isFolder();
    }

    @Override
    public String toString() {
        return timestamp + "|" + type + "|" + path;
    }
}
