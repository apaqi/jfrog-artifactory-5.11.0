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

package org.artifactory.rest.common.service.artifact;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.properties.ArtifactPropertiesAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.artifact.BaseArtifact;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Deprecated //TODO [by dan][sha2]:remove after migration-enforcing major
public class AddSha256ToArtifactService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(AddSha256ToArtifactService.class);

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BaseArtifact baseArtifact = (BaseArtifact) request.getImodel();
        String repoKey = baseArtifact.getRepoKey();
        String path = baseArtifact.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);

        // TODO: [by dan] This half-baked check is placed here because it's a last minute change - need to include a
        // TODO: [by dan] thorough check for recursive calls! (RTFACT-8474)
        if(!authService.canDeployToLocalRepository() || !authService.canDeploy(repoPath)
                || !authService.canAnnotate(repoPath)) {
            String authErr = "User '" + authService.currentUsername() + "' is missing the required permissions to " +
                    "trigger a sha256 calculation on path '" + repoPath.toPath() + "'";
            log.error(authErr);
            response.error(authErr);
            return;
        }
         try {
            addSha256ToArtifact(repoPath);
        } catch (IOException e) {
           log.error("error adding sha256 property");
        } catch (RepoRejectException e) {
            log.error("error adding sha256 property");
             response.error("error adding sha256 property");
        }
    }


    /**
     * add sha256 to repo path
     *
     * @param repoPath - artifact repo path
     */
    private void addSha256ToArtifact(RepoPath repoPath) throws IOException, RepoRejectException {
        ContextHelper.get().beanForType(AddonsManager.class).addonByType(ArtifactPropertiesAddon.class)
                .addPropertySha256RecursivelyMultiple(repoPath);
    }
}
