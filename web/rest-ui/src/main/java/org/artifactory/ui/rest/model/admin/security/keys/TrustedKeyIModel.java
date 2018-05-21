package org.artifactory.ui.rest.model.admin.security.keys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.model.BaseModel;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Inbar Tal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustedKeyIModel extends BaseModel {
    @JsonProperty
    private String kid;
    @JsonProperty("public_key")
    private String trustedKey;
    @JsonProperty
    private String alias;
    @JsonProperty
    private String fingerprint;
    @JsonProperty
    private Long issued;
    @JsonProperty
    private String issuedBy;
    @JsonProperty
    private Long expiry;
}
