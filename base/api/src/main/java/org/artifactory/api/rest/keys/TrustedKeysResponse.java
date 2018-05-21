package org.artifactory.api.rest.keys;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author Rotem Kfir
 */
@ToString
public class TrustedKeysResponse {

    @JsonProperty
    private final List<TrustedKeyResponse> keys = Lists.newArrayList();

    @Nonnull
    public List<TrustedKeyResponse> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    public TrustedKeysResponse keys(@Nullable List<TrustedKeyResponse> keys) {
        this.keys.clear();
        if (keys != null) {
            this.keys.addAll(keys);
        }
        return this;
    }
}
