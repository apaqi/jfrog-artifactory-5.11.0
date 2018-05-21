package org.artifactory.rest.common.model.xray;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Yuval Reches
 */
public class XrayAllowBlockedDownloadModel extends BaseModel {

    private boolean xrayAllowBlocked;

    public XrayAllowBlockedDownloadModel(boolean xrayIgnoreBlocked) {
        this.xrayAllowBlocked = xrayIgnoreBlocked;
    }

    public XrayAllowBlockedDownloadModel() { }

    public boolean isXrayAllowBlocked() {
        return xrayAllowBlocked;
    }

    public void setXrayAllowBlocked(boolean xrayAllowBlocked) {
        this.xrayAllowBlocked = xrayAllowBlocked;
    }
}
