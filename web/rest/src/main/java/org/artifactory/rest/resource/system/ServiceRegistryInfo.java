package org.artifactory.rest.resource.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegistryInfo {

    @com.fasterxml.jackson.annotation.JsonProperty("service_id")
    @JsonProperty("service_id")
    private String id;

    private String url;
}
