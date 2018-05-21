package org.artifactory.api.rest.distribution.bundle.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@Accessors(fluent = true)
@NoArgsConstructor
public class BundleTransactionResponse {
    @JsonProperty("tx_path")
    String transactionPath;
}
