package org.artifactory.addon.license;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Status class for license installation, removal and activation
 *
 * @author Shay Bagants
 */
public class LicenseOperationStatus {

    // Total number of licenses that were candidates to add into the license pool
    private int totalCandidates = 0;

    // Map of formatted license with it's validation status result
    private Map<String, AddRemoveLicenseVerificationResult> addRemoveLicenseValidationResults = Maps.newHashMap();

    // Map of license activation result. Key is valid license key, value is Active
    private Map<String, VerificationResult> licenseActivationResult = Maps.newHashMap();
    private Exception error;

    public void putLicenseAddRemoveStatus(String licenseKey, AddRemoveLicenseVerificationResult result) {
        addRemoveLicenseValidationResults.put(licenseKey, result);
    }

    public void putLicenseActivationStatus(String licenseKey, VerificationResult result) {
        licenseActivationResult.put(licenseKey, result);
    }

    public Map<String, AddRemoveLicenseVerificationResult> getAddRemoveLicenseVerificationResult() {
        return addRemoveLicenseValidationResults;
    }

    public Map<String, VerificationResult> getLicenseActivationResult() {
        return licenseActivationResult;
    }

    public void merge(LicenseOperationStatus toMerge) {
        Map<String, AddRemoveLicenseVerificationResult> addRemoveResults = toMerge.getAddRemoveLicenseVerificationResult();
        addRemoveResults.forEach((k, v) -> addRemoveLicenseValidationResults.put(k, v));

        Map<String, VerificationResult> activationResult = toMerge.getLicenseActivationResult();
        activationResult.forEach((k, v) -> licenseActivationResult.put(k, v));
    }

    public boolean hasValidLicenses() {
        return addRemoveLicenseValidationResults.entrySet().stream()
                .anyMatch(entry -> entry.getValue().isValid());
    }

    public Set<String> getValidLicenses() {
        return addRemoveLicenseValidationResults.entrySet().stream()
                .filter(map -> map.getValue().isValid())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void setException(Exception error) {
        this.error = error;
    }

    public boolean hasException() {
        return error != null;
    }

    public Exception getException() {
        return error;
    }
}
