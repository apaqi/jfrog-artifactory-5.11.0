package org.artifactory.storage.db.keys.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.codehaus.jackson.annotate.JsonCreator;

import static org.jfrog.common.ArgUtils.requireNonBlank;
import static org.jfrog.common.ArgUtils.requireSatisfies;

/**
 * @author Rotem Kfir
 */
@Data
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class DbTrustedKey {

    @NonNull private String kid;
    @NonNull private String trustedKey;
    @NonNull private String fingerprint;
    private String alias;
    private Long issued;
    private String issuedBy;
    private Long expiry;

    @JsonCreator
    public DbTrustedKey(String kid, String trustedKey, String fingerprint, String alias, Long issued,
            String issuedBy, Long expiry) {
        validate(kid, trustedKey, fingerprint, alias, issued, issuedBy, expiry);
    }

    public void validate() {
        validate(kid, trustedKey, fingerprint, alias, issued, issuedBy, expiry);
    }

    public void validate(String kid, String trustedKey, String fingerprint, String alias, Long issued, String issuedBy, Long expiry) {
        this.kid = requireNonBlank(kid, "key ID is required");
        this.trustedKey = requireNonBlank(trustedKey, "Trusted key is required");
        this.fingerprint = requireNonBlank(fingerprint, "Fingerprint is required");
        this.alias = alias;
        this.issued = requireSatisfies(issued, v -> (v == null || v > 0), "Issued time must be positive");
        this.issuedBy = issuedBy;
        this.expiry = requireSatisfies(expiry, v -> (v == null || v == 0 || (v > 0 && (issued == null || v > issued))), "Expiry should be after issue date");
    }
}
