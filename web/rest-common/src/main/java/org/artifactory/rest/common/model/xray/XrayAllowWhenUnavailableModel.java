package org.artifactory.rest.common.model.xray;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Yuval Reches
 */
public class XrayAllowWhenUnavailableModel extends BaseModel {

    private boolean xrayAllowWhenUnavailable;

    public XrayAllowWhenUnavailableModel(boolean xrayAllowWhenUnavailable) {
        this.xrayAllowWhenUnavailable = xrayAllowWhenUnavailable;
    }

    public XrayAllowWhenUnavailableModel() {}

    public boolean isXrayAllowWhenUnavailable() {
        return xrayAllowWhenUnavailable;
    }

    public void setXrayAllowWhenUnavailable(boolean xrayAllowWhenUnavailable) {
        this.xrayAllowWhenUnavailable = xrayAllowWhenUnavailable;
    }
}
