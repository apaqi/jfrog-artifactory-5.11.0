package org.artifactory.storage.db.bundle.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import static org.jfrog.common.ArgUtils.requireNonBlank;
import static org.jfrog.common.ArgUtils.requirePositive;


@Data
@NoArgsConstructor
public class BundleNode {
    long id;
    long nodeId;
    String repoPath;
    long bundleId;

    public void validate() {
        validate(nodeId, bundleId);
    }

    public void validate(long nodeId, long bundleId) {
        this.nodeId = requirePositive(nodeId, "nodeId must be positive");
        this.bundleId = requirePositive(bundleId, "bundleId must be positive");
        this.repoPath = requireNonBlank(repoPath, "nodePath must not be blank");
    }
}
