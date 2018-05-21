package org.artifactory.ui.rest.model.artifacts.search.versionsearch.result;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author: ortalh
 */
@Data
public class VersionNativeResult {

    private static final Logger log = LoggerFactory.getLogger(VersionNativeResult.class);
    private String name;
    private String packageId;
    private String repoKey;
    private Date lastModified;
    private long size;

    //Don't remove this, the test needs it
    public VersionNativeResult() {

    }

    public VersionNativeResult(String name, String packageId, String repoKey, Date lastModified) {
        this.name = name;
        this.packageId = packageId;
        this.repoKey = repoKey;
        this.lastModified = lastModified;
    }
}