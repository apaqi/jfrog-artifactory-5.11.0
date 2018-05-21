package org.artifactory.storage.db.bundle.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BundleBlob {
    long id;
    String data;
    long bundleId;
}
