package org.artifactory.rest.resource.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationProviderInfo {

    @com.fasterxml.jackson.annotation.JsonProperty("owner_service_id")
    @JsonProperty("owner_service_id")
    private String ownerServiceId;

    @com.fasterxml.jackson.annotation.JsonProperty("service_id")
    @JsonProperty("service_id")
    private String serviceId;

    private String url;

    private String token;

    private String certificate;

    private String type;
}
