package org.artifactory.repo.cache.expirable;

import org.artifactory.descriptor.repo.RepoType;

/**
 * @author Inbar Tal
 */
public interface MetaDataChecker {
    /**
     * Indicates whether the specified path is metadata path
     * @param repoType
     * @param repoKey
     * @param path the path to check
     * @return true if the path is metadata, false otherwise
     */
    boolean isMetadata(RepoType repoType, String repoKey, String path);
}
