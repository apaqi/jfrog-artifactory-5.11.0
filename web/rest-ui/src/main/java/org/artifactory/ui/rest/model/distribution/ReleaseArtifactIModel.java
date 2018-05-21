package org.artifactory.ui.rest.model.distribution;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@NoArgsConstructor
public class ReleaseArtifactIModel {
    String name;
    String path;
    String created;
    long size;
    @JsonProperty("component_name")
    String componentName;
    @JsonProperty("component_version")
    String componentVersion;
}
