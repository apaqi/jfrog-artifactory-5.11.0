package org.artifactory.environment.converter.local.version.v5;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.environment.converter.local.version.MarkerFileConverter;
import org.artifactory.version.ArtifactoryVersion;

import java.io.File;

/**
 * @author Noam Shemesh
 */
public class V5100AddAccessDecryptAllUsersMarkerFile extends MarkerFileConverter {
    @Override
    protected ArtifactoryVersion getVersionToUpgrade() {
        return ArtifactoryVersion.v5100m009;
    }

    @Override
    protected File getMarkerFile(ArtifactoryHome artifactoryHome) {
        return artifactoryHome.getAccessUserCustomDataDecryptionMarkerFile();
    }
}
