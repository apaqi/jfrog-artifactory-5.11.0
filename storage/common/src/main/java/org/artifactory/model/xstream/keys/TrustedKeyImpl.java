package org.artifactory.model.xstream.keys;

import lombok.*;
import org.artifactory.keys.TrustedKey;

import java.util.Objects;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class TrustedKeyImpl implements TrustedKey {
    @NonNull private String kid;
    @NonNull private String trustedKey;
    @NonNull private String fingerprint;
    private String alias;
    private Long issued;
    private String issuedBy;
    private Long expiry;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrustedKeyImpl)) {
            return false;
        }
        TrustedKeyImpl that = (TrustedKeyImpl) o;
        return Objects.equals(kid, that.kid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kid);
    }
}
