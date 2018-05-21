package org.artifactory.ui.rest.service.distribution;

import org.apache.commons.lang3.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersionsResponse;
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
import java.util.stream.Collectors;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllReleaseBundleVersionsService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(GetAllReleaseBundleVersionsService.class);

    @Autowired
    AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        String bundleName = request.getPathParamByKey("name");
        BundleVersionsResponse bundles = releaseBundleAddon.getBundleVersions(bundleName);
        bundles.setVersions(
                bundles.getVersions().stream().filter(version -> "complete".equalsIgnoreCase(version.getStatus()))
                        .collect(
                                Collectors.toSet()));

        try {
            response.iModel(JacksonWriter.serialize(bundles));
        } catch (IOException e) {
            log.error("Failed to serialize response ", e);
            throw new IllegalArgumentException(e);
        }
    }
}
