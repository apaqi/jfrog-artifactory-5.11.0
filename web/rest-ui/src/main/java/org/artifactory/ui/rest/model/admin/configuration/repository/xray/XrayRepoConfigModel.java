package org.artifactory.ui.rest.model.admin.configuration.repository.xray;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_XRAY_INDEX;

/**
 * @author Dan Feldman
 */
public class XrayRepoConfigModel implements RestModel {

    private boolean enabled = DEFAULT_XRAY_INDEX;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
