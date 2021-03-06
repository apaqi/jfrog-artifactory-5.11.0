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

package org.artifactory.ui.rest.service.artifacts.deploy;

import com.sun.jersey.multipart.FormDataMultiPart;
import org.artifactory.api.artifact.ArtifactInfo;
import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.DeployService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.ArtifactoryRequestBase;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadedArtifactInfo;
import org.artifactory.ui.utils.MultiPartUtils;
import org.artifactory.ui.utils.TreeUtils;
import org.artifactory.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactMultiDeployService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ArtifactMultiDeployService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    DeployService deployService;

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getQueryParamByKey("repoKey");
        String path = request.getQueryParamByKey("path");
        LocalRepoDescriptor localRepo = resolveLocalRepo(repoKey);
        if (localRepo == null){
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Must specify Local repository or a virtual repository with local default deployment repository");
            return;
        }
        RepoPath localRepoPath = RepoPathFactory.create(localRepo.getKey(), path);
        if (!authorizationService.canDeploy(localRepoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user " + authorizationService.currentUsername());
            return;
        }
        List<String> fileNames = saveFileToTempFolder(request, response);
        if (!fileNames.isEmpty()) {
            //deploy file
            String savedUniqueFileName = fileNames.get(0);
            String actualFileName = fileNames.get(1);
            deploy(savedUniqueFileName, actualFileName, response, localRepo.getKey(), path);
        }
    }

    private LocalRepoDescriptor resolveLocalRepo(String repoKey) {
        RepoDescriptor repoDescriptor = repositoryService.repoDescriptorByKey(repoKey);
        if (repoDescriptor instanceof VirtualRepoDescriptor) {
            return((VirtualRepoDescriptor) repoDescriptor).getDefaultDeploymentRepo();
        }
        else if (repoDescriptor instanceof LocalRepoDescriptor){
            return (LocalRepoDescriptor) repoDescriptor;
        }
        return null;

    }

    /**
     * upload file to temp folder
     *
     * @param artifactoryRequest - encapsulate data related to request
     * @param response           - encapsulate data require for response
     * @return - true if uploaded successful
     */
    private List<String> saveFileToTempFolder(ArtifactoryRestRequest artifactoryRequest, RestResponse response) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        List<String> fileNames = new ArrayList<>();
        try {
            MultiPartUtils.createTempFolderIfNotExist(uploadDir);
            // get upload model
            UploadArtifactInfo uploadArtifactInfo = (UploadArtifactInfo) artifactoryRequest.getImodel();
            // save file data tto temp
            FormDataMultiPart formDataMultiPart = uploadArtifactInfo.fetchFormDataMultiPart();
            String fileName = formDataMultiPart.getFields().get("file").get(0).getContentDisposition().getFileName();
            MultiPartUtils.saveFileDataToTemp(centralConfigService, formDataMultiPart, uploadDir,
                    fileNames, true);
            fileNames.add(fileName);
        } catch (Exception e) {
            response.error(e.getMessage());
        }
        return fileNames;
    }

    /**
     * deploy file to repository
     *
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void deploy(String savedUniqueFileName, String actualFileName, RestResponse artifactoryResponse,
            String repoKey, String path) {
        String uploadDir = ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath();
        Properties properties = parseMatrixParams(actualFileName);
        LocalRepoDescriptor localRepoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        try {
            File file = new File(uploadDir, savedUniqueFileName);
            UnitInfo unitInfo = new ArtifactInfo(path);
            // deploy file
            deployService.deploy(localRepoDescriptor, unitInfo, file, properties);
            // delete tmp file
            Files.removeFile(file);
            boolean showUrl = TreeUtils.shouldProvideTreeLink(localRepoDescriptor, unitInfo.getPath());
            UploadedArtifactInfo uploadedArtifactInfo = new UploadedArtifactInfo(showUrl, repoKey, unitInfo.getPath());
            artifactoryResponse.iModel(uploadedArtifactInfo);
        } catch (RepoRejectException e) {
            log.error(e.toString());
            artifactoryResponse.error(DeployUtil.getDeployError(actualFileName, repoKey, e));
        }
    }

    /**
     * get uploaded file Properties
     *
     * @param fileName - file name
     * @return
     */
    private Properties parseMatrixParams(String fileName) {
        Properties props;
        props = (Properties) InfoFactoryHolder.get().createProperties();
        String targetPathFieldValue = fileName;
        int matrixParamStart = targetPathFieldValue.indexOf(Properties.MATRIX_PARAMS_SEP);
        if (matrixParamStart > 0) {
            ArtifactoryRequestBase.processMatrixParams(props, targetPathFieldValue.substring(matrixParamStart));
        }
        return props;
    }
}
