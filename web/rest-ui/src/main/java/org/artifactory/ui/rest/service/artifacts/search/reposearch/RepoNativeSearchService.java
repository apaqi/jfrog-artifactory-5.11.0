package org.artifactory.ui.rest.service.artifacts.search.reposearch;

import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.SearchNativeRepoResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RepoNativeSearchService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RepoNativeSearchService.class);

    private static final String TYPE = "type";
    @Autowired
    private RepositoryService repoService;
    @Autowired
    AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepoType repoType;
        String type = request.getPathParamByKey(TYPE);
        if (type != null) {
            repoType = RepoType.fromType(type);
        } else {
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        SearchNativeRepoResult model = search(repoType);
        response.iModel(model);
    }

    public SearchNativeRepoResult search(RepoType repoType) {
        List<LocalRepoDescriptor> repoSet = Lists.newArrayList();
        List<LocalRepoDescriptor> localAndCachedRepoDescriptors = repoService.getLocalAndCachedRepoDescriptors();
        localAndCachedRepoDescriptors.addAll(repoService.getDistributionRepoDescriptors());
        localAndCachedRepoDescriptors.stream()
                .filter(descriptor -> descriptor.getType().equals(repoType))
                .filter(descriptor -> authorizationService
                        .canRead(InternalRepoPathFactory.repoRootPath(descriptor.getKey())))
                .forEach(repoSet::add);
        List<String> repoKeys = Lists.newArrayList();
        for (LocalRepoDescriptor descriptor : repoSet) {
            repoKeys.add(descriptor.getKey());
        }
        // update response data
        return new SearchNativeRepoResult(repoKeys, repoKeys.size());
    }
}
