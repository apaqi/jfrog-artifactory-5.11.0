package org.artifactory.api.blob.infos;

import org.artifactory.api.rest.blob.BlobInfo;
import org.jfrog.storage.StorageException;

import java.util.List;

/**
 * @author Inbar Tal
 */

public interface BlobInfosDBService {

    /**
     * Insert a given blobInfo to DB. If already exist - override it.
     * @param checksum - the {@link BlobInfo} id
     * @param blobInfo - the {@link BlobInfo} to cache
     * @return true if cached successfully, otherwise return false
     * @throws StorageException - In case a storage error occurred during the operation
     */
    boolean putBlobInfo(String checksum, BlobInfo blobInfo) throws StorageException;

    /**
     * Delete a blobInfo by it's checksum.
     * @param checksum - the checksum of the {@link BlobInfo} to delete
     * @return 1 if delete successfully, otherwise return 0
     * @throws StorageException - In case a storage error occurred during the operation
     */
    int deleteBlobInfo(String checksum) throws StorageException;

    /**
     * Delete all given blobInfos by their checksums.
     * @param checksums - the checksum of the blobInfo to delete
     * @return number of {@link BlobInfo}s deleted
     * @throws StorageException - In case a storage error occurred during the operation
     */
    int deleteBlobInfos(List<String> checksums) throws StorageException;

    /**
     * Find blobInfo by it's checksum.
     * @param checksum - the checksum of the requested blobInfo
     * @return - the {@link BlobInfo} if found, otherwise return null.
     * @throws StorageException - In case a storage error occurred during the operation
     */
    BlobInfo getBlobInfo(String checksum) throws StorageException;

}
