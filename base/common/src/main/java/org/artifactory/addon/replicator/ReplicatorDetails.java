package org.artifactory.addon.replicator;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import static org.jfrog.common.ArgUtils.requireNonBlank;

/**
 * @author Yoaz Menda
 */
@Data
@Builder
public class ReplicatorDetails {

    @NonNull
    private String externalBaseUrl;
    @NonNull
    private String internalBaseUrl;

    public ReplicatorDetails(String externalBaseUrl, String internalBaseUrl) {
        validate(externalBaseUrl, internalBaseUrl);
        this.externalBaseUrl = externalBaseUrl;
        this.internalBaseUrl = internalBaseUrl;
    }

    public ReplicatorDetails() {
    }

    private void validate(String localReplicatorExternalUrl, String localReplicatorInternalUrl) {
        requireNonBlank(localReplicatorExternalUrl, "External replicator url must not be null");
        requireNonBlank(localReplicatorInternalUrl, "Internal replicator url must not be null");
    }
}