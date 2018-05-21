package org.artifactory.api.rest.distribution.bundle.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ArtifactProperty {
    String key;
    List<String> values;
}
