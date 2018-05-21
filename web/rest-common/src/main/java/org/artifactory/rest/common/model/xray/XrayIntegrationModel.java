package org.artifactory.rest.common.model.xray;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Yuval Reches
 */
public class XrayIntegrationModel extends BaseModel {
    private boolean xrayEnabled;
    private boolean xrayAllowBlocked;
    private boolean xrayAllowWhenUnavailable;

    public XrayIntegrationModel(boolean xrayEnabled, boolean xrayAllowBlocked, boolean xrayAllowWhenUnavailable) {
        this.xrayEnabled = xrayEnabled;
        this.xrayAllowBlocked = xrayAllowBlocked;
        this.xrayAllowWhenUnavailable = xrayAllowWhenUnavailable;
    }

    public XrayIntegrationModel() {
    }

    public boolean isXrayEnabled() {
        return xrayEnabled;
    }

    public void setXrayEnabled(boolean xrayEnabled) {
        this.xrayEnabled = xrayEnabled;
    }

    public boolean isXrayAllowBlocked() {
        return xrayAllowBlocked;
    }

    public void setXrayAllowBlocked(boolean xrayIgnoreBlocked) {
        this.xrayAllowBlocked = xrayIgnoreBlocked;
    }

    public boolean isXrayAllowWhenUnavailable() {
        return xrayAllowWhenUnavailable;
    }

    public void setXrayAllowWhenUnavailable(boolean xrayAllowWhenOffline) {
        this.xrayAllowWhenUnavailable = xrayAllowWhenOffline;
    }
}
