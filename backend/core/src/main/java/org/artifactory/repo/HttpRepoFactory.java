package org.artifactory.repo;

import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;

public interface HttpRepoFactory {

    HttpRepo build(HttpRepoDescriptor repoDescriptor, boolean offlineMode, RemoteRepo oldRemoteRepo);

    RepoType getRepoType();
}
