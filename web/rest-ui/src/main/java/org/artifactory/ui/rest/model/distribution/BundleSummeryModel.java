package org.artifactory.ui.rest.model.distribution;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BundleSummeryModel {
    String name;
    String latestVersion;
    String created;
}
