package org.artifactory.component;

import org.artifactory.api.component.ComponentDetails;
import org.artifactory.api.component.ComponentDetailsFetcher;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ComponentDetailsFetcherImpl implements ComponentDetailsFetcher {

    @Autowired
    private Map<String, RepoComponentDetailsFetcher> componentDetailsFetchers;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public ComponentDetails calcComponentDetails(RepoPath repoPath) {
        if (!repoPath.isFile()) {
            throw new IllegalArgumentException("File not found:" + repoPath);
        }
        String repoKey = repoPath.getRepoKey();
        RepoDescriptor repoDescriptor = repositoryService.repoDescriptorByKey(repoKey);
        String type = repoDescriptor.getType().getType();
        RepoComponentDetailsFetcher fetcher = componentDetailsFetchers.get(type + "ComponentDetailsFetcher");
        if (fetcher == null) {
            return new ComponentDetails();
        }
        return fetcher.calcComponentDetails(repoPath);
    }
}
