package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.docker.DockerAddon;
import org.artifactory.addon.docker.DockerV2InfoModel;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @author: ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DockerV2ManifestBaseService {
    private static final Logger log = LoggerFactory.getLogger(DockerV2ManifestBaseService.class);
    private static final int SKIP_DIGEST_SHA256 = 17;
    private static final int PACKAGEID_LEN = 12;
    public static final String DIGEST_SHA2_PREFIX = "sha256:";

    @Autowired
    private RepositoryService repoService;

    @Autowired
    CentralConfigService configService;

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private RepositoryService repositoryService;

    public ItemInfo getManifest(String repoKey, String path, RestResponse response) {
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        if (!repoService.exists(repoPath)) {
            response.error("path " + repoKey + "/" + path + " is not exist");
            response.responseCode(404);
            log.error("Unable to find Docker manifest under '" + repoKey + "/" + path + "'");
            return null;
        }
        if (repoService.isVirtualRepoExist(repoKey)) {
            repoPath = repoService.getVirtualItemInfo(repoPath).getRepoPath();
        }
        if (!authorizationService.canRead(repoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user " + authorizationService.currentUsername());
            return null;
        }
        ItemInfo manifest = repoService.getChildren(repoPath)
                .stream().filter(itemInfo -> itemInfo.getName().equals("manifest.json"))
                .findFirst().orElseGet(null);

        if (manifest == null) {
            response.error("Unable to find Docker manifest under '" + repoKey + "/" + path);
            response.responseCode(404);
            log.error("Unable to find Docker manifest under '" + repoKey + "/" + path + "'");
        }
        return manifest;
    }

    public DockerV2InfoModel getDockerV2Info(ItemInfo manifest, String repoKey, String path) {
        DockerV2InfoModel dockerV2Info = null;
        try {
            dockerV2Info = ContextHelper.get().beanForType(AddonsManager.class)
                    .addonByType(DockerAddon.class).getDockerV2Model(manifest.getRepoPath(), false);
        } catch (Exception e) {
            String err = "Unable to extract Docker metadata for '" + repoKey + "/" + path + "'";
            log.error(err);
            log.debug(err, e);
        }
        return dockerV2Info;
    }

    /**
     * Image ID as the client shows it is defined to be the digest of the config layer.
     * Apart from the ability to compare this to the string you see in the docker images list this value has no usage.
     *
     * @link https://windsock.io/explaining-docker-image-ids/
     */
    public String getImageId(ItemInfo manifest) {
        String fileContent = repositoryService.getStringContent((FileInfo) manifest);
        String packageId = "";
        int intIndex = fileContent.indexOf("\"config\": {");
        if(intIndex != -1) {
            int digestIndex = fileContent.indexOf("digest", intIndex) + SKIP_DIGEST_SHA256;
            packageId = fileContent.substring(digestIndex, digestIndex + PACKAGEID_LEN);
        }
        return packageId;
    }

    /**
     * The digest or sha256 properties retrieved from the manifest (which we tag upon deployment or remote download)
     * can be used to pull the image.
     *
     * @link https://docs.docker.com/engine/reference/commandline/pull/#pull-an-image-by-digest-immutable-identifier
     */
    protected String getPackageIdFromDigestProperty(String digest) {
        String normalized = null;
        if (StringUtils.isNotBlank(digest)) {
            normalized = digest.replace(DIGEST_SHA2_PREFIX, "");
        }
        return normalized == null ? "" : normalized;
    }
}
