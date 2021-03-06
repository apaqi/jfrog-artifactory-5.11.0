package org.artifactory.api.keys;

import org.artifactory.keys.TrustedKey;
import org.artifactory.sapi.common.Lock;
import org.jfrog.storage.StorageException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * @author Rotem Kfir
 */
public interface TrustedKeysService {
    /**
     * Find all Trusted Keys
     *
     * @return a list of keys
     * @throws StorageException In case a storage error occurred during the operation
     */
    @Nonnull
    List<TrustedKey> findAllTrustedKeys() throws StorageException;

    /**
     * Find a Trusted Key by its ID
     *
     * @param kid the id by which to find the Trusted Key
     * @return the Trusted Key
     * @throws StorageException In case a storage error occurred during the operation
     */
    @Nonnull
    Optional<TrustedKey> findTrustedKeyById(@Nonnull String kid) throws StorageException;

    /**
     * Find a Trusted Key by its alias
     *
     * @param alias the alias by which to find the Trusted Key (case sensitive)
     * @return the Trusted Key
     * @throws StorageException In case a storage error occurred during the operation
     */
    @Nonnull
    Optional<TrustedKey> findTrustedKeyByAlias(@Nonnull String alias) throws StorageException;

    /**
     * Create a Trusted Key
     *
     * @param key the Trusted Key to save
     * @return the saved Trusted Key
     *
     * @throws StorageException In case a storage error occurred during the operation
     */
    @Lock
    @Nonnull
    TrustedKey createTrustedKey(TrustedKey key) throws StorageException;

    /**
     * Deletes a Trusted Key by its id
     *
     * @param kid the id by which to delete the Trusted Key
     * @return true if a Trusted Key was deleted, false if the Trusted Key was not found
     * @throws StorageException In case a storage error occurred during the operation
     */
    @Lock
    boolean deleteTrustedKey(String kid) throws StorageException;

    /**
     * Deletes all Trusted Keys
     *
     * @return the number of deleted rows
     * @throws StorageException In case a storage error occurred during the operation
     */
    @Lock
    long deleteAllTrustedKeys() throws StorageException;
}
