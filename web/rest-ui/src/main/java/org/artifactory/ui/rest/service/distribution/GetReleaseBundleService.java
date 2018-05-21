package org.artifactory.ui.rest.service.distribution;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.component.ComponentDetails;
import org.artifactory.api.component.ComponentDetailsFetcher;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseBundleModel;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.exception.BadRequestException;
import org.artifactory.ui.rest.model.distribution.ReleaseArtifactIModel;
import org.artifactory.ui.rest.model.distribution.ReleaseBundleIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetReleaseBundleService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(GetReleaseBundleService.class);

    @Autowired
    AddonsManager addonsManager;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    ComponentDetailsFetcher componentDetailsFetcher;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        String name = request.getPathParamByKey("name");
        String version = request.getPathParamByKey("version");
        ReleaseBundleModel bundle = releaseBundleAddon.getBundleModel(name, version);
        if (!"complete".equalsIgnoreCase(bundle.getStatus())) {
            throw new BadRequestException(name + ":" + version + " bundle is incomplete");
        }
        ReleaseBundleIModel imodel = new ReleaseBundleIModel();
        populateModel(bundle, imodel);
        try {
            response.iModel(JacksonWriter.serialize(imodel));
        } catch (IOException e) {
            log.error("Failed to serialize response ", e);
            throw new IllegalArgumentException(e);
        }
    }

    private void populateModel(ReleaseBundleModel bundle, ReleaseBundleIModel imodel) {
        imodel.setCreated(bundle.getCreated());
        imodel.setDesc(bundle.getDescription());
        imodel.setName(bundle.getName());
        imodel.setVersion(bundle.getVersion());
        imodel.setSignature(bundle.getSignature());
        imodel.setStatus(bundle.getStatus());

        bundle.getArtifacts().forEach(artifact -> {
            ReleaseArtifactIModel artifactIModel = new ReleaseArtifactIModel();
            RepoPath repoPath = RepoPathFactory.create(artifact.getTargetRepoPath());
            FileInfo fileInfo = repositoryService.getFileInfo(repoPath);
            artifactIModel.setSize(fileInfo.getSize());
            artifactIModel.setCreated(fileInfo.getCreatedBy());
            artifactIModel.setName(fileInfo.getName());
            artifactIModel.setPath(artifact.getTargetRepoPath());

            try {
                ComponentDetails componentDetails = componentDetailsFetcher.calcComponentDetails(repoPath);
                artifactIModel.setComponentName(componentDetails.name());
                artifactIModel.setComponentVersion(componentDetails.version());
            } catch(Exception e) {
                log.info("Could not get component details for artifact '" + artifactIModel.getPath() + "'. " + e.getMessage());
            }
            imodel.getArtifacts().add(artifactIModel);
        });

        long totalSize = imodel.getArtifacts().stream().mapToLong(ReleaseArtifactIModel::getSize).sum();
        imodel.setSize(totalSize);
    }
}
