package org.artifactory.api.rest.distribution.bundle.utils;

import com.google.common.base.Splitter;

import java.util.List;

public class BundleNameVersionResolver {
    private String bundleName;
    private String bundleVersion;

    public BundleNameVersionResolver(String transactionPath) {
        List<String> pathParts = getPathParts(transactionPath);
        if (pathParts.size() < 3){
            throw new IllegalArgumentException("Invalid transaction path");
        }
        bundleName = pathParts.get(1);
        bundleVersion = pathParts.get(2);
    }

    public String getBundleName() {
        return bundleName;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    private List<String> getPathParts(String transactionPath) {
        return Splitter.on("/").splitToList(transactionPath);
    }

}