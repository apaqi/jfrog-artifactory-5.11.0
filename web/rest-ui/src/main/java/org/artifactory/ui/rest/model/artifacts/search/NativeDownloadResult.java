package org.artifactory.ui.rest.model.artifacts.search;

import lombok.Data;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.springframework.stereotype.Component;

/**
 * @author: ortalh
 */
@Data
@Component
public class NativeDownloadResult implements RestModel {
    int totalDownloads;

    public NativeDownloadResult(int totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public NativeDownloadResult(){}

    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
