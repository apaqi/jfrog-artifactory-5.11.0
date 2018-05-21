package org.artifactory.api.rest.distribution.bundle.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class BundleVersion implements Comparable{
    String version;
    String created;
    String status;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BundleVersion)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        BundleVersion that = (BundleVersion) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(created, that.created) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), version, created, status);
    }

    @Override
    public int compareTo(Object o) {
        return this.version.compareTo(((BundleVersion)o).version);
    }
}
