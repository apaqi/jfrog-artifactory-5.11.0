package org.artifactory.ui.rest.model.artifacts.search;

import lombok.Data;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeResult;

import java.util.Collection;


/**
 * @author: ortalh
 */
@Data
public class SearchNativePackageResult implements RestModel {
    private Collection<PackageNativeResult> results;
    private long resultsCount;

    //Don't remove this, the test needs it
    public SearchNativePackageResult() {

    }

    public SearchNativePackageResult(Collection<PackageNativeResult> results, long resultsCount) {
        this.results = results;
        this.resultsCount = resultsCount;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
