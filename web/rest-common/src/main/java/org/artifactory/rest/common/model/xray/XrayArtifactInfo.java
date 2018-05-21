package org.artifactory.rest.common.model.xray;

/**
 * @author Yuval Reches
 */
public class XrayArtifactInfo {

    private String xrayIndexStatus;
    private String xrayIndexStatusLastUpdatedTimestamp;
    private Boolean xrayBlocked;
    private String xrayBlockReason;
    private Boolean allowBlockedArtifacts;

    public XrayArtifactInfo(){}

    public String getXrayIndexStatus() {
        return xrayIndexStatus;
    }

    public void setXrayIndexStatus(String xrayIndexStatus) {
        this.xrayIndexStatus = xrayIndexStatus;
    }

    public String getXrayIndexStatusLastUpdatedTimestamp() {
        return xrayIndexStatusLastUpdatedTimestamp;
    }

    public void setXrayIndexStatusLastUpdatedTimestamp(String xrayIndexStatusLastUpdatedTimestamp) {
        this.xrayIndexStatusLastUpdatedTimestamp = xrayIndexStatusLastUpdatedTimestamp;
    }

    public Boolean getXrayBlocked() {
        return xrayBlocked;
    }

    public void setXrayBlocked(Boolean xrayBlocked) {
        this.xrayBlocked = xrayBlocked;
    }

    public String getXrayBlockReason() {
        return xrayBlockReason;
    }

    public void setXrayBlockReason(String xrayBlockReason) {
        this.xrayBlockReason = xrayBlockReason;
    }

    public Boolean getAllowBlockedArtifacts() {
        return allowBlockedArtifacts;
    }

    public void setAllowBlockedArtifacts(Boolean allowBlockedArtifacts) {
        this.allowBlockedArtifacts = allowBlockedArtifacts;
    }
}
