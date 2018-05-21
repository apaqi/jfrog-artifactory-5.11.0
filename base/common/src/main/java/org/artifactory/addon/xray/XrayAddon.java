package org.artifactory.addon.xray;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EdgeBlockedAddon;
import org.artifactory.api.repo.Async;
import org.artifactory.exception.UnsupportedOperationException;
import org.artifactory.repo.RepoPath;
import org.jfrog.build.api.Build;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.List;

/**
 * @author Chen Keinan
 */
@EdgeBlockedAddon
public interface XrayAddon extends Addon {

    //The user that's set up in Artifactory to allow Xray access to various resources
    String ARTIFACTORY_XRAY_USER = "xray";

    boolean isXrayConfigMissing();

    /**
     * @return True if Xray is configured in descriptor and enabled
     */
    boolean isXrayEnabled();

    /**
     * @return True if Xray client reached Xray server and is in stable mode
     */
    boolean isXrayAlive();

    /**
     * @return A message that explains why Xray is not alive, null otherwise (in case there is no problem)
     */
    @Nullable
    String getOfflineMessage();

    /**
     * @return True if Xray server is running with a compatible version with Xray client
     */
    boolean isXrayVersionValid();

    CloseableHttpResponse scanBuild(XrayScanBuild xrayScanBuild) throws IOException;

    String createXrayUser();

    void removeXrayConfig();

    void deleteXrayUser(String xrayUser);

    void setXrayEnabledOnRepos(List<XrayRepo> repos, boolean enabled);

    /**
     * Get the list of repositories that should be enabled with 'xrayIndex'
     *
     * @param repos The list of repositories that should be enabled with 'xrayIndex'
     */
    void updateXraySelectedIndexedRepos(List<XrayRepo> repos);

    /**
     * Get the number of artifact that are potential for xray index
     *
     * @param repoKey The repository to search on
     * @return The count result
     */
    int getXrayPotentialCountForRepo(String repoKey) throws UnsupportedOperationException;

    /**
     * Cleans all "xray.*" properties from DB asynchronously
     */
    void cleanXrayPropertiesFromDB();

    /**
     * Calls the Xray client to invalidate all it's caches
     */
    void cleanXrayClientCaches();

    /**
     * List of repositories that are of type that is supported by Xray server connected to Artifactory.
     * Can be called only if Xray integration is enabled and connected to Xray.
     * Otherwise list will be empty (warn message will be printed).
     * @param indexed look for repos that are marked enabled Xray or not
     */
    List<XrayRepo> getXrayIndexedAndNonIndexed(boolean indexed);

    /**
     * Sends Xray event for a new build
     * @param build holds the build name and number
     */
    void callAddBuildInterceptor(Build build);

    /**
     * Sends Xray event for build deletion
     * @param buildName Build Name
     * @param buildNumber Build Number
     */
    void callDeleteBuildInterceptor(String buildName, String buildNumber);

    @Async
    void indexRepos(List<String> repos);

    void clearAllIndexTasks();

    void blockXrayGlobally();

    void unblockXrayGlobally();

    @Nonnull
    ArtifactXrayInfo getArtifactXrayInfo(@Nonnull RepoPath path);

    /**
     * @return true if {@param path} should be blocked for download according to its storing repo's xray configuration.
     */
    boolean isDownloadBlocked(RepoPath path);

    /**
     * true if xray can index this file
     */
    boolean isHandledByXray(RepoPath path);

    /**
     * Sets alert ignore on {@param path} via Xray client
     * It will ignore the current alert only.
     */
    boolean setAlertIgnored(RepoPath path);

    boolean isAllowBlockedArtifactsDownload();

    boolean updateAllowBlockedArtifactsDownload(boolean allow);

    boolean updateAllowDownloadWhenUnavailable(boolean allow);

    /**
     * @return true if {@param folder} should be blocked for download:
     * we return true in case there is at least one file under the folder that is blocked for download in Xray
     */
    boolean isBlockedFolder(RepoPath folder);

    /**
     * @return true if there is xray config for repo and it is enabled and Xray global integration is enabled
     *         false otherwise
     */
    boolean isXrayConfPerRepoAndIntegrationEnabled(String repoKey);
}
