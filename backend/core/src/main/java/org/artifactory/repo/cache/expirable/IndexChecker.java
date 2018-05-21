package org.artifactory.repo.cache.expirable;

import org.artifactory.descriptor.repo.RepoType;

/**
 * @author Inbar Tal
 */
public abstract class IndexChecker implements MetaDataChecker, CacheExpirable {

    @Override
    public boolean isMetadata(RepoType repoType, String repoKey, String path) {
        return isExpirable(repoType, repoKey, path);
    }
}
