package org.artifactory.ui.rest.model.artifacts.search;

import lombok.Data;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionNativeResult;

import java.util.Date;
import java.util.List;

/**
 * @author: ortalh
 */
@Data
public class SearchNativeVersionListResult implements RestModel {
    private String packageName;
    private Date lastModified;
    private List<VersionNativeResult> versions;
    private long resultsCount;

    public SearchNativeVersionListResult(List<VersionNativeResult> results, long resultsCount, String packageName) {
        if (resultsCount > 0) {
            this.packageName = packageName;
            this.versions = results;
            this.resultsCount = resultsCount;
        }
    }

    //Don't remove this, the test needs it
    public SearchNativeVersionListResult() {

    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}

