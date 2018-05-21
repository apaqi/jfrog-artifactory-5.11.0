package org.artifactory.api.rest.distribution.bundle.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReleaseArtifact {

    @JsonProperty("repo_path")
    String targetRepoPath;
    String checksum;
    List<ArtifactProperty> props = Lists.newArrayList();

}
