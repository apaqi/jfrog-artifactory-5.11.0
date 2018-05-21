package org.artifactory.component;

import org.artifactory.api.component.ComponentDetails;
import org.artifactory.repo.RepoPath;

public interface RepoComponentDetailsFetcher {

    ComponentDetails calcComponentDetails(RepoPath repoPath);

}
