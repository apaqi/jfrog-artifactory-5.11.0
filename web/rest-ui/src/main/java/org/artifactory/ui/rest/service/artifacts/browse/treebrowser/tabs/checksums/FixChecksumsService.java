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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.checksums;

import org.apache.http.HttpStatus;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FixChecksumsService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(FixChecksumsService.class);

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BaseArtifactInfo artifactInfo = (BaseArtifactInfo) request.getImodel();
        RepoPath path = RepoPathFactory.create(artifactInfo.getRepoKey(), artifactInfo.getPath());
        if(repoService.isVirtualRepoExist(path.getRepoKey())){
            path = repoService.getVirtualFileInfo(path).getRepoPath();
        }
        if (!authService.canDeploy(path) || authService.isAnonymous()) {
            log.debug("User {} attempting to fix checksums on path {} has insufficient rights",
                    authService.currentUsername(), path);
            response.error("Insufficient rights for operation").responseCode(HttpStatus.SC_UNAUTHORIZED);
        } else {
            try {
                log.debug("Fixing checksums for {}", path);
                repoService.fixChecksums(path);
                response.info("Successfully fixed checksum inconsistency");
            } catch (Exception e) {
                response.error("Could not fix checksum inconsistency");
                log.debug("", e);
            }
        }
    }
}
