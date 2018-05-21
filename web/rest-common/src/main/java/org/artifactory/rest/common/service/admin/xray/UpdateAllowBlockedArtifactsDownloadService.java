package org.artifactory.rest.common.service.admin.xray;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.rest.common.model.xray.XrayAllowBlockedDownloadModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Yuval Reches
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateAllowBlockedArtifactsDownloadService implements RestService {

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        XrayAllowBlockedDownloadModel allow = (XrayAllowBlockedDownloadModel) request.getImodel();
        if (!addonsManager.addonByType(XrayAddon.class).updateAllowBlockedArtifactsDownload(allow.isXrayAllowBlocked())) {
            response.error("Encountered error on config changes. Check logs for more information.");
        }
    }
}
