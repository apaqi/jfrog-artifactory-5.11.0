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

package org.artifactory.storage.fs.session;

import org.artifactory.repo.RepoPath;
import org.artifactory.storage.fs.lock.FsItemLockEntry;
import org.artifactory.storage.fs.lock.LockEntryId;
import org.artifactory.storage.fs.lock.SessionLockEntry;
import org.artifactory.storage.tx.SessionResource;
import org.springframework.transaction.support.TransactionSynchronization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Represents a thread bound storage session in a context of a transaction.
 *
 * @author Yossi Shaul
 */
public interface StorageSession {

    UUID getSessionId();

    /**
     * Saves the changes to the underlying storage. Save is called before a transaction is committed.
     */
    void save();

    /**
     * Acquire a write lock on the entry if not already locked in the current session.
     *
     * @param lockEntryId The lock entry id holding the lock and the repo path
     * @return A write locked item entry
     */
    @Nonnull
    FsItemLockEntry writeLock(LockEntryId lockEntryId);

    /**
     * Removes and unlocks the entry for this repo path.
     *
     * @param repoPath The repo path to unlock
     * @return True if the session held a lock entry on this repo path
     */
    boolean removeLockEntry(RepoPath repoPath);

    /**
     * Returns the session lock entry for the given repo path.
     *
     * @param repoPath The repo path to get the lock entry for
     * @return The session lock entry for the given repo path. Null if non exist
     */
    @Nullable
    SessionLockEntry getLockEntry(RepoPath repoPath);

    <T extends SessionResource> T getOrCreateResource(Class<T> resourceClass);

    //TODO: [by YS] move the methods below to another interface - ManagedStorageSession. Only to be called by the synchronizer

    /**
     * Releases all the resources held by this session. This includes releasing locks.
     */
    void releaseResources();

    void beforeCommit();

    /**
     * Called after the transaction finished - might have been committed or rolled back. No further modifications to
     * the database should be performed at this point.
     *
     * @see TransactionSynchronization#afterCompletion(int)
     */
    void afterCompletion(boolean success);
}
