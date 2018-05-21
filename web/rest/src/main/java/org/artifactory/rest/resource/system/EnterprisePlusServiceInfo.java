package org.artifactory.rest.resource.system;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnterprisePlusServiceInfo {

    @com.fasterxml.jackson.annotation.JsonProperty("auth_provider")
    @JsonProperty("auth_provider")
    private AuthProviderInfo authProviderInfo;

    @com.fasterxml.jackson.annotation.JsonProperty("service_registry")
    @JsonProperty("service_registry")
    private ServiceRegistryInfo serviceRegistryInfo;
}
