package org.artifactory.addon.go;

import org.artifactory.addon.Addon;
import org.artifactory.repo.RepoPath;

/**
 * @author Liz Dashevski
 */
public interface GoAddon extends Addon {

    /**
     * Used to extract go metadata for a given go package
     */
    default GoMetadataInfo getGoMetadataToUiModel(RepoPath repoPath) { return null; }

    /**
     * Determines if a file is go (.zip)
     */
    default boolean isGoFile(RepoPath repoPath) { return false; }
}
