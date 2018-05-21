package org.artifactory.ui.rest.service.distribution;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteReleaseBundleService implements RestService {

    @Autowired
    AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        String name = request.getPathParamByKey("name");
        String version = request.getPathParamByKey("version");
        Boolean includeContent = Boolean.valueOf(request.getQueryParamByKey("include_content"));
        releaseBundleAddon.deleteReleaseBundle(name, version, includeContent);
        response.responseCode(HttpStatus.SC_OK);
    }
}
