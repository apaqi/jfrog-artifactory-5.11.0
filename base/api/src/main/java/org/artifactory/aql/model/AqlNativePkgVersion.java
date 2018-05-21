package org.artifactory.aql.model;

import lombok.Data;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;

/**
 * @author: ortalh
 */
@Data
public class AqlNativePkgVersion {
    private AqlBaseFullRowImpl aqlBaseFullRow;
    private String version;

    public AqlNativePkgVersion(AqlBaseFullRowImpl aqlBaseFullRow, String version) {
        this.aqlBaseFullRow = aqlBaseFullRow;
        this.version = version;
    }
}
