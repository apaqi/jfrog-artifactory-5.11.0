package org.artifactory.rest.common.model.xray;

import org.artifactory.addon.xray.ArtifactXrayInfo;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;

/**
 * Converts internal artifact Xray model to REST model
 *
 * @author Yuval Reches
 */
public abstract class ArtifactXrayModelHelper {

    public static XrayArtifactInfo getXrayInfo(ArtifactXrayInfo artifactXrayInfo) {
        XrayArtifactInfo xrayArtifactInfo = new XrayArtifactInfo();
        // Updating the model
        String indexStatus = artifactXrayInfo.getIndexStatus();
        if (indexStatus == null) {
            return xrayArtifactInfo;
        }
        xrayArtifactInfo.setXrayIndexStatus(indexStatus);
        xrayArtifactInfo.setXrayBlocked(artifactXrayInfo.isBlocked());
        xrayArtifactInfo.setXrayBlockReason(artifactXrayInfo.getBlockReason());
        Long indexLastUpdated = artifactXrayInfo.getIndexLastUpdated();
        CentralConfigService centralConfig = ContextHelper.get().getCentralConfig();
        xrayArtifactInfo.setXrayIndexStatusLastUpdatedTimestamp(
                indexLastUpdated == null ? null : centralConfig.format(indexLastUpdated));
        return xrayArtifactInfo;
    }

}
