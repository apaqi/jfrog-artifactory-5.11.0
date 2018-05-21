package org.artifactory.rest.resource.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * See <a href="https://tools.ietf.org/html/rfc6749#section-5.1">RFC6749 - Section 5.1</a>
 * @author Yinon Avraham
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseModel {

    @com.fasterxml.jackson.annotation.JsonProperty("access_token")
    @JsonProperty("access_token")
    private String accessToken;

    @com.fasterxml.jackson.annotation.JsonProperty("refresh_token")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @com.fasterxml.jackson.annotation.JsonProperty("expires_in")
    @JsonProperty("expires_in")
    private Long expiresIn;

    @com.fasterxml.jackson.annotation.JsonProperty("scope")
    @JsonProperty("scope")
    private String scope;

    @com.fasterxml.jackson.annotation.JsonProperty("token_type")
    @JsonProperty("token_type")
    private String tokenType;
}
