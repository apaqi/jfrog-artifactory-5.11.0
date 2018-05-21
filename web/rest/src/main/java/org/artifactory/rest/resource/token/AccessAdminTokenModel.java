package org.artifactory.rest.resource.token;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessAdminTokenModel {

    @JsonProperty("service_id")
    private String serviceId;

}
