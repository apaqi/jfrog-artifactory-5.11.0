package org.artifactory.environment.converter.local.version;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.environment.converter.BasicEnvironmentConverter;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;

import java.io.File;
import java.io.IOException;

/**
 * @author Noam Shemesh
 */
public abstract class MarkerFileConverter implements BasicEnvironmentConverter {
    @Override
    public boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        return (source.getVersion().before(getVersionToUpgrade()) &&
                target.getVersion().afterOrEqual(getVersionToUpgrade()));
    }

    @Override
    public void convert(ArtifactoryHome artifactoryHome, CompoundVersionDetails source, CompoundVersionDetails target) {
        try {
            // write this file on non-ha env, or on HA primary node.
            if (!artifactoryHome.isHaConfigured() ||
                    (artifactoryHome.getHaNodeProperties() != null && artifactoryHome.getHaNodeProperties().isPrimary())) {
                FileUtils.write(getMarkerFile(artifactoryHome), "");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write access emigrate marker file", e);
        }
    }

    @Override
    public void assertConversionPreconditions(ArtifactoryHome home) throws ConverterPreconditionException {
    }

    protected abstract ArtifactoryVersion getVersionToUpgrade();

    protected abstract File getMarkerFile(ArtifactoryHome artifactoryHome);

    public static void convertIfExistsAndDeleteMarkerFile(File markerFile, Runnable converter) {
        if (markerFile.exists()) {
            converter.run();
            if (!markerFile.delete()) {
                throw new IllegalStateException("Could not delete marker file! " +
                        "File must be deleted for startup to complete. [" + markerFile.getAbsolutePath() + "]");
            }
        }
    }
}
