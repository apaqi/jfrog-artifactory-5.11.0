package org.artifactory.api.rest.distribution.bundle.models;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class BundleVersionsResponse {

    Set<BundleVersion> versions = Sets.newHashSet();

    public void add(BundleVersion version){
        versions.add(version);
    }
}
