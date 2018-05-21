package org.artifactory.rest.resource.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemServiceInfo {

    private String id;
    private String version;
    private String type;
    private String url;

    @com.fasterxml.jackson.annotation.JsonProperty("enterprise_plus")
    @JsonProperty("enterprise_plus")
    private EnterprisePlusServiceInfo enterprisePlusInfo;

}
