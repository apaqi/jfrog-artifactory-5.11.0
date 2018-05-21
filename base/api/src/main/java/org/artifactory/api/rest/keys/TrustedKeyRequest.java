package org.artifactory.api.rest.keys;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonProperty;

import static org.jfrog.common.ArgUtils.requireNonBlank;

/**
 * @author Rotem Kfir
 */
@Data
@NoArgsConstructor
@ToString
public class TrustedKeyRequest {

    @JsonProperty("key")
    private String trustedKey;

    @JsonProperty("public_key")
    private String publicTrustedKey;

    @JsonProperty
    private String alias;

    public TrustedKeyRequest(String trustedKey, String alias) {
        this.trustedKey = requireNonBlank(trustedKey, "Trusted key is required");
        this.alias = alias;
    }

    public String getTrustedKey() {
        if (trustedKey != null) {
            return trustedKey;
        }
        return publicTrustedKey;
    }

    public String getAlias() {
        return alias;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor
    public static class Builder {
        private String trustedKey;
        private String alias;

        public Builder setTrustedKey(String trustedKey) {
            this.trustedKey = trustedKey;
            return this;
        }

        public Builder setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public TrustedKeyRequest build() {
            return new TrustedKeyRequest(trustedKey, alias);
        }
    }
}
