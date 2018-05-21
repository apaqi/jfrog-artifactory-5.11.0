package org.artifactory.api.release.bundle;

import org.artifactory.sapi.common.Lock;
import org.jfrog.storage.StorageException;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Rotem Kfir
 */
public interface ReleaseBundleService {
    /**
     * Find all Release Bundles with incomplete transaction that has been created over a predefined time period ago
     *
     * @return a list of Release Bundles ids
     * @throws StorageException In case a storage error occurred during the operation
     */
    @Nonnull
    List<Long> findExpiredBundlesIds() throws StorageException;

    /**
     * Deletes a Release Bundle either with a closed or incomplete transaction by its id, along with its files and blob
     *
     * @param id the id by which to delete the Release Bundle
     * @param deleteContent if true then artifacts related to this bundle will be deleted from the repositories
     * @return true if a Release Bundle was deleted, false if the Release Bundle was not found
     * @throws StorageException In case a storage error occurred during the operation
     */
    @Lock
    boolean deleteBundle(Long id, boolean deleteContent) throws StorageException;

    /**
     * Redistributes a Release Bundle with an incomplete transaction by its id
     *
     * @param id the id by which to find the Release Bundle
     * @return true if a Release Bundle was updated, false if the Release Bundle was not found or is not incomplete
     * @throws StorageException In case a storage error occurred during the operation
     */
    boolean redistributeBundle(Long id) throws StorageException;
}
