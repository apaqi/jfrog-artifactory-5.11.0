package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.docker.DockerAddon;
import org.artifactory.addon.docker.DockerV2InfoModel;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ViewArtifact;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.DockerNativeV2InfoRequest;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy.AqlUIDockerV2ImageSearchStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author: ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DockerV2ManifestNativeService extends DockerV2ManifestBaseService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(DockerV2ManifestNativeService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getPathParamByKey("repoKey");
        String packageName = request.getQueryParamByKey("packageName");
        String versionName = request.getQueryParamByKey("versionName");

        if (StringUtils.isEmpty(repoKey) || StringUtils.isEmpty(packageName) || StringUtils.isEmpty(versionName)) {
            response.error("repoKey or packageName or versionName is missing");
            response.responseCode(400);
            log.error("RepoKey or packageName or versionName is missing");
            return;
        }
        String path = packageName + "/" + versionName;
        boolean returnManifest = Boolean.parseBoolean(request.getQueryParamByKey("manifest"));

        //TODO [by shayb]: sorry for that, the native search uses strategy that searches images by name, or by "library/name", this is a quick fix for the 5.10 release
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        if (!repositoryService.exists(repoPath)) {
            path = AqlUIDockerV2ImageSearchStrategy.PREFIX + path;
        }
        ItemInfo manifest = getManifest(repoKey, path, response);
        if (manifest == null) {
            return;
        }
        if (returnManifest) {
            String maifestString = getManifestAsString(manifest);
            ViewArtifact viewArtifact = new ViewArtifact();
            viewArtifact.setFileContent(maifestString);
            response.iModel(viewArtifact);
            return;
        }
        try {
            String packageId = getImageId(manifest);
            DockerV2InfoModel dockerV2Info = ContextHelper.get().beanForType(AddonsManager.class)
                    .addonByType(DockerAddon.class).getDockerV2Model(manifest.getRepoPath(), false);

            DockerNativeV2InfoRequest dockerNativeV2InfoRequest = new DockerNativeV2InfoRequest();
            dockerNativeV2InfoRequest.setBlobsInfo(dockerV2Info.blobsInfo);
            dockerNativeV2InfoRequest.setSize(dockerV2Info.tagInfo.totalSizeLong);
            dockerNativeV2InfoRequest.setName(versionName);
            dockerNativeV2InfoRequest.setPackageName(packageName);
            dockerNativeV2InfoRequest.setLastModified(manifest.getLastModified());
            dockerNativeV2InfoRequest.setPackageId(packageId);
            response.iModel(dockerNativeV2InfoRequest);
        } catch (Exception e) {
            String err = "Unable to extract Docker metadata for '" + repoKey + "/" + path + "'";
            response.error(err);
            log.error(err);
            log.debug(err, e);
        }
    }

    private String getManifestAsString(ItemInfo manifest) {
        return repositoryService.getStringContent((FileInfo) manifest);
    }
}
