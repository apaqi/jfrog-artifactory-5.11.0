package org.artifactory.rest.resource.system;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.artifactory.rest.common.model.RestModel;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OpenidInfoResponse implements RestModel {
    @JsonProperty("login_url")
    private String loginUrl;
    @JsonProperty("logout_url")
    private String logoutUrl;
    @JsonProperty("token_url")
    private String tokenUrl;
    private boolean enabled;
}
