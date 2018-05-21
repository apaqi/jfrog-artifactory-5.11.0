package org.artifactory.api.component;

import org.artifactory.repo.RepoPath;

public interface ComponentDetailsFetcher {

    ComponentDetails calcComponentDetails(RepoPath repoPath);
}
