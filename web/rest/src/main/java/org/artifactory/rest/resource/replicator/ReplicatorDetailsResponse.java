package org.artifactory.rest.resource.replicator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReplicatorDetailsResponse {
    private String externalUrl;
}
