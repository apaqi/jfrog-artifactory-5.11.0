package org.artifactory.api.rest.distribution.bundle.models;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Data
@NoArgsConstructor
public class BundlesResponse {
    Map<String, Set<BundleVersion>> bundles = new HashMap<>();

    public void add(String bundleName, BundleVersion versions) {
        Set<BundleVersion> bundleVersions = Optional.ofNullable(bundles.get(bundleName)).orElseGet(Sets::newHashSet);
        bundleVersions.add(versions);
        bundles.put(bundleName, bundleVersions);
    }
}
