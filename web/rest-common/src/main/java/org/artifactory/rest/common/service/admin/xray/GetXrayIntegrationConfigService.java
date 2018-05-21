package org.artifactory.rest.common.service.admin.xray;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.repo.XrayDescriptor;
import org.artifactory.rest.common.model.xray.XrayIntegrationModel;
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
public class GetXrayIntegrationConfigService implements RestService {

    @Autowired
    private CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean enabled = false;
        boolean allowBlocked = false;
        boolean allowWhenUnavailable = false;
        XrayDescriptor xrayConfig = configService.getDescriptor().getXrayConfig();
        if (xrayConfig != null) {
            enabled = xrayConfig.isEnabled();
            allowBlocked = xrayConfig.isAllowBlockedArtifactsDownload();
            allowWhenUnavailable = xrayConfig.isAllowDownloadsXrayUnavailable();
        }
        XrayIntegrationModel xrayIntegrationModel = new XrayIntegrationModel(enabled, allowBlocked, allowWhenUnavailable);
        response.iModel(xrayIntegrationModel);
    }

}
