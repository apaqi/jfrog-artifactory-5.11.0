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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.repo.config.RepoConfigDefaultValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.MavenTypeSpecificConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.CreateRepositoryConfigService;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Serves the remote search tab, when no JCenter repo is configured in Artifactory and user has opted to
 * create one automatically.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateDefaultJcenterRepoService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CreateDefaultJcenterRepoService.class);

    private static final String DEFAULT_JCENTER_NAME = "jcenter";
    private static final String MAVEN_2_DEFAULT_LAYOUT = "maven-2-default";

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private CreateRepositoryConfigService createRepoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        log.debug("Creating the default JCenter remote repository");
        String repoKey = guessNameForJcenter();
        if (StringUtils.isBlank(repoKey)) {
            response.error("repositories with the name 'jcenter' and 'jcenter-default' already exist, can't resolve " +
                    "a proper name for the new repo.").responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        RemoteRepositoryConfigModel model = createDefaultModel(repoKey);
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        try {
            configDescriptor = createRepoService.createRepo(response, model, configDescriptor);
            if (configDescriptor != null) {
                configService.saveEditedDescriptorAndReload(configDescriptor);
                response.info("Successfully created repository '" + repoKey + "'");
            }
        } catch (RepoConfigException e) {
            log.error("Error while creating repository " + repoKey);
        }
    }

    private RemoteRepositoryConfigModel createDefaultModel(String repoKey) {
        CentralConfigDescriptor configDescriptor = configService.getDescriptor();
        RemoteRepositoryConfigModel model = new RemoteRepositoryConfigModel();
        GeneralRepositoryConfigModel general = new GeneralRepositoryConfigModel();
        general.setRepoKey(repoKey);
        RemoteBasicRepositoryConfigModel basic = new RemoteBasicRepositoryConfigModel();
        basic.setUrl(RepoConfigDefaultValues.MAVEN_GROUP_URL); //jcenter
        if (configDescriptor.getRepoLayout(MAVEN_2_DEFAULT_LAYOUT) != null) {
            basic.setLayout(MAVEN_2_DEFAULT_LAYOUT);
            basic.setRemoteLayoutMapping(MAVEN_2_DEFAULT_LAYOUT);
        } else {
            basic.setLayout(RepoConfigDefaultValues.DEFAULT_REPO_LAYOUT);
        }
        RemoteAdvancedRepositoryConfigModel advanced = new RemoteAdvancedRepositoryConfigModel();
        if (configDescriptor.getPropertySets().stream()
                .anyMatch(propertySet -> propertySet.getName().equalsIgnoreCase("artifactory"))) {
            advanced.setPropertySets(Lists.newArrayList(new PropertySetNameModel("artifactory", 0)));
        }
        ProxyDescriptor defaultProxy = configDescriptor.getDefaultProxy();
        if (defaultProxy != null) {
            advanced.getNetwork().setProxy(defaultProxy.getKey());
        }
        MavenTypeSpecificConfigModel typeSpecific = new MavenTypeSpecificConfigModel();
        model.setGeneral(general);
        model.setBasic(basic);
        model.setAdvanced(advanced);
        model.setTypeSpecific(typeSpecific);
        return model;
    }

    private String guessNameForJcenter() {
        String jcenterDef = DEFAULT_JCENTER_NAME + "-default";
        Set<String> remoteRepoKeys = configService.getDescriptor().getLocalRepositoriesMap().keySet();
        if (!remoteRepoKeys.contains(DEFAULT_JCENTER_NAME)) {
            return DEFAULT_JCENTER_NAME;
        } else if (!remoteRepoKeys.contains(jcenterDef)) {
            return jcenterDef;
        } else {
            for (int i = 1; i < 5; i++) {
                String suggestedName = jcenterDef + '-' + i;
                if (!remoteRepoKeys.contains(suggestedName)) {
                    return suggestedName;
                }
            }
        }
        return null;
    }
}