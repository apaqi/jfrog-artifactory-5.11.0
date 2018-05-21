package org.artifactory.environment.converter.local.version.v4;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.environment.converter.local.version.MarkerFileConverter;
import org.artifactory.version.ArtifactoryVersion;

import java.io.File;

/**
 * @author Noam Shemesh
 */
public class V560AddAccessEmigrateMarkerFile extends MarkerFileConverter {
    @Override
    protected ArtifactoryVersion getVersionToUpgrade() {
        return ArtifactoryVersion.v560m001;
    }

    @Override
    protected File getMarkerFile(ArtifactoryHome artifactoryHome) {
        return artifactoryHome.getAccessEmigrateMarkerFile();
    }
}
