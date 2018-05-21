package org.artifactory.ui.rest.service.distribution;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersion;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.distribution.BundleSummeryModel;
import org.artifactory.ui.rest.model.distribution.ReleaseBundlesIModel;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllReleaseBundleService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(GetAllReleaseBundleService.class);

    @Autowired
    AddonsManager addonsManager;


    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        Map<String, Set<BundleVersion>> bundles = releaseBundleAddon.getAllBundles().getBundles();
        ReleaseBundlesIModel iModel = new ReleaseBundlesIModel();
        populateModel(bundles, iModel);
        try {
            response.iModel(JacksonWriter.serialize(iModel));
        } catch (IOException e) {
            log.error("Failed to serialize response ", e);
            throw new IllegalArgumentException(e);
        }
    }

    private void populateModel(Map<String, Set<BundleVersion>> bundles, ReleaseBundlesIModel iModel) {
        bundles.forEach((bundleName, versions) -> {
            Optional<BundleVersion> latest = versions.stream()
                    .filter(version -> "complete".equalsIgnoreCase(version.getStatus())).sorted(Comparator
                            .comparing(v -> ISODateTimeFormat.dateTimeParser().withZoneUTC()
                                    .parseDateTime(v.getCreated())))
                    .findFirst();
            if (latest.isPresent()) {
                BundleVersion bundleVersion = latest.get();
                BundleSummeryModel bundleSummeryModel = new BundleSummeryModel();
                bundleSummeryModel.setCreated(bundleVersion.getCreated());
                bundleSummeryModel.setLatestVersion(bundleVersion.getVersion());
                bundleSummeryModel.setName(bundleName);
                iModel.getBundles().add(bundleSummeryModel);
            }
        });
    }
}
