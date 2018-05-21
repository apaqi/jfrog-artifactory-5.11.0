package org.artifactory.ui.rest.model.artifacts.search;

import lombok.Data;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.List;

/**
 * @author: ortalh
 */
@Data
public class SearchNativeRepoResult implements RestModel {
    private List<String> results;
    private long resultsCount;

    public SearchNativeRepoResult(List<String> repoKeys, long resultsCount) {
        this.results = repoKeys;
        this.resultsCount = resultsCount;
    }

    //Don't remove this, the test needs it
    public SearchNativeRepoResult(){}

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
