package org.artifactory.addon.xray;

/**
 * Xray specific information for artifacts.
 *
 * @author Yinon Avraham
 */
public interface ArtifactXrayInfo {

    String getIndexStatus();

    Long getIndexLastUpdated();

    boolean isArtifactProcessed();

    // Xray sets this on artifacts that should be blocked - depending on it's own watch for this repo\path
    boolean isBlocked();

    String getBlockReason();

    ArtifactXrayInfo EMPTY = new ArtifactXrayInfo() {

        @Override
        public String getIndexStatus() {
            return null;
        }

        @Override
        public Long getIndexLastUpdated() {
            return null;
        }

        @Override
        public boolean isArtifactProcessed() {
            return false;
        }

        @Override
        public boolean isBlocked() {
            return false;
        }

        @Override
        public String getBlockReason() { return null; }
    };
}
