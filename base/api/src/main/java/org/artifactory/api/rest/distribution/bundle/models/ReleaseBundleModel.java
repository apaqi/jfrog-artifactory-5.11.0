package org.artifactory.api.rest.distribution.bundle.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

@Data
@NoArgsConstructor
public class ReleaseBundleModel {

    String name;
    String version;
    String created;
    String description;
    List<ReleaseArtifact> artifacts;
    String signature;
    @JsonIgnore
    long bundleId;
    @JsonIgnore
    String status;

}
