package org.artifactory.rest.common.model.xray;

import org.artifactory.rest.common.model.BaseModel;

/**
 * Model that represent a repository index state.
 * Potential is all the artifacts that can be indexed
 * (@see XrayHandler#extensionsForIndex)
 */
public class XrayRepoIndexStatsModel extends BaseModel {
    private long potential = 0;

    public XrayRepoIndexStatsModel() {
    }

    public XrayRepoIndexStatsModel(long potential) {
        this.potential = potential;
    }

    public long getPotential() {
        return potential;
    }

    public void setPotential(long potential) {
        this.potential = potential;
    }
}
