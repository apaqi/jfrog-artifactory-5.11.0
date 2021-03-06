/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.ui.rest.service.admin.configuration.repositories;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigModelBuilder;
import org.artifactory.util.stream.BiOptional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetRepositoryConfigService implements RestService {

    private static final String LOCAL = "local";
    private static final String REMOTE = "remote";
    private static final String VIRTUAL = "virtual";
    private static final String DISTRIBUTION = "distribution";

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RepoConfigModelBuilder configBuilder;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoType = request.getPathParamByKey("repoType");
        String repoKey = request.getPathParamByKey("repoKey");
        List<? extends RepoDescriptor> repoDescList = getAllRepositoriesDescriptors(repoType);
        if (StringUtils.isBlank(repoKey)) {
            List<RepositoryConfigModel> repositoryConfigModels =
                    repoDescList.stream()
                            .map(repoDesc -> createModelFromDescriptor(repoType, repoDesc))
                            .collect(Collectors.toList());

            response.iModelList(repositoryConfigModels)
                    .responseCode(HttpStatus.SC_OK);
        } else {
            BiOptional.of(repoDescList.stream()
                    .filter(repoDesc -> repoDesc.getKey().equals(repoKey))
                    .findFirst())
                    .ifPresent(repoDesc -> response.iModel(createModelFromDescriptor(repoType, repoDesc)))
                    .ifNotPresent(() -> response.error("Repository '" + repoKey + "' does not exists.")
                            .responseCode(HttpStatus.SC_NOT_FOUND));
        }
    }

    private RepositoryConfigModel createModelFromDescriptor(String repoType, RepoDescriptor repoDesc) {
        return getRepoDescriptorByType(repoType).fromDescriptor(configBuilder, repoDesc);
    }

    private List<? extends RepoBaseDescriptor> getAllRepositoriesDescriptors(String repoType) {
        List<? extends RepoBaseDescriptor> descriptors = Lists.newArrayList();
        switch (repoType) {
            case LOCAL:
                descriptors = repositoryService.getLocalRepoDescriptors();
                break;
            case REMOTE:
                descriptors = repositoryService.getRemoteRepoDescriptors();
                break;
            case VIRTUAL:
                descriptors = repositoryService.getVirtualRepoDescriptors();
                break;
            case DISTRIBUTION:
                descriptors = repositoryService.getDistributionRepoDescriptors();
                break;
        }
        return descriptors;
    }

    private RepositoryConfigModel getRepoDescriptorByType(String repoType) {
        RepositoryConfigModel configModel = null;
        switch (repoType) {
            case LOCAL:
                configModel = new LocalRepositoryConfigModel();
                break;
            case REMOTE:
                configModel = new RemoteRepositoryConfigModel();
                break;
            case VIRTUAL:
                configModel = new VirtualRepositoryConfigModel();
                break;
            case DISTRIBUTION:
                configModel = new DistributionRepositoryConfigModel();
                break;
        }
        return configModel;
    }
}
