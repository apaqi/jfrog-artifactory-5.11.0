package org.artifactory.addon.release.bundle;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EnterprisePlusAddon;
import org.artifactory.api.rest.distribution.bundle.models.BundleTransactionResponse;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersionsResponse;
import org.artifactory.api.rest.distribution.bundle.models.BundlesResponse;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseBundleModel;
import org.artifactory.api.rest.release.ReleaseBundleRequest;
import org.artifactory.api.rest.release.ReleaseBundleResult;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.common.Lock;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author Shay Bagants
 */
@EnterprisePlusAddon
public interface ReleaseBundleAddon extends Addon {

    ReleaseBundleResult executeReleaseBundleRequest(ReleaseBundleRequest bundleRequest, boolean includeMetaData);

    void verifyReleaseBundleSignature(String signature, String keyID);

    @Lock
    BundleTransactionResponse createBundleTransaction(String signedJwsBundle) throws ParseException, IOException;

    @Lock
    void closeBundleTransaction(String transactionId);

    BundlesResponse getAllBundles();

    BundleVersionsResponse getBundleVersions(String bundleName);

    String getBundleJson(String bundleName, String bundleVersion);

    String getBundleSignedJws(String bundleName, String bundleVersion);

    void exportTo(ExportSettings exportSettings);

    @Lock
    void importFrom(ImportSettings importSettings);

    @Lock
    void deleteAllBundles();

    @Lock
    void deleteReleaseBundle(String bundleName, String bundleVersion, boolean includeContent);

    ReleaseBundleModel getBundleModel(String bundleName, String bundleVersion);
}
