package org.artifactory.rest.resource.system;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.model.RestModel;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Noam Shemesh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenExchangeRequest implements RestModel {
    @JsonProperty("grant_type")
    private String grantType;
    private String code;
    @JsonProperty("redirect_uri")
    private String redirectUri;
}
