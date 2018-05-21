package org.artifactory.ui.rest.model.artifacts.search.versionsearch.result;

import lombok.Data;
import org.artifactory.addon.docker.DockerBlobInfoModel;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.List;

/**
 * @author: ortalh
 */
@Data
public class DockerNativeV2InfoRequest implements RestModel {
    private String name;
    private String packageName;
    private long lastModified;
    private long size;
    private String packageId;
    private List<DockerBlobInfoModel> blobsInfo;

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
