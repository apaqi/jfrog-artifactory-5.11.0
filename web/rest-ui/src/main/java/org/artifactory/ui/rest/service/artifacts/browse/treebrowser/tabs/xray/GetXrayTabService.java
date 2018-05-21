package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.xray;


import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.ArtifactXrayInfo;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.xray.XrayArtifactInfo;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

import static org.artifactory.rest.common.model.xray.ArtifactXrayModelHelper.getXrayInfo;

/**
 * Service for fetching the Xray metadata for the UI
 *
 * @author Yuval Reches
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetXrayTabService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetXrayTabService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AddonsManager addonsManager;

    private XrayAddon xrayAddon;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        xrayAddon = addonsManager.addonByType(XrayAddon.class);
        RepoPath repoPath = RequestUtils.getPathFromRequest(request);

        String errorMsg;
        if (!authorizationService.canRead(repoPath)) {
            errorMsg = "Forbidden UI REST call from user " + authorizationService.currentUsername();
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).error(errorMsg).buildResponse();
            log.error(errorMsg);
            return;
        } else if (!xrayAddon.isXrayConfPerRepoAndIntegrationEnabled(repoPath.getRepoKey())
                || !xrayAddon.isHandledByXray(repoPath)) {
            errorMsg = "Forbidden UI REST: Xray is not configured on the repo '" + repoPath.getRepoKey() + "' or file" +
                    " '" + repoPath.getPath() + "' is not handled by Xray";
            response.responseCode(HttpServletResponse.SC_BAD_REQUEST).error(errorMsg).buildResponse();
            log.error(errorMsg);
            return;
        }

        // Retrieving info from Xray client
        ArtifactXrayInfo artifactXrayInfo;
        try {
            artifactXrayInfo = xrayAddon.getArtifactXrayInfo(repoPath);
        } catch (IllegalStateException e) {
            response.error(e.getMessage()).buildResponse();
            return;
        }
        if (artifactXrayInfo.getIndexStatus() == null) {
            response.error("Xray is not available").buildResponse();
            return;
        }
        XrayArtifactInfo xrayArtifactInfo = getXrayInfo(artifactXrayInfo);
        updateAllowBlockedArtifacts(xrayArtifactInfo);
        // update response
        response.iModel(xrayArtifactInfo);
    }

    private void updateAllowBlockedArtifacts(XrayArtifactInfo xrayArtifactInfo) {
        if (xrayArtifactInfo.getXrayBlocked()) {
            xrayArtifactInfo.setAllowBlockedArtifacts(xrayAddon.isAllowBlockedArtifactsDownload());
        }
    }
}
