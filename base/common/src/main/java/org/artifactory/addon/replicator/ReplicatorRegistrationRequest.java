package org.artifactory.addon.replicator;

import lombok.*;

/**
 * @author Yoaz Menda
 */
@Data
@Builder
@ToString
@AllArgsConstructor
public class ReplicatorRegistrationRequest {

    @NonNull
    private String externalBaseUrl;
    @NonNull
    private String internalBaseUrl;

    public ReplicatorRegistrationRequest(){}

}