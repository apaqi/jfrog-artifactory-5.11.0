package org.artifactory.ui.rest.model.distribution;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

@Data
@NoArgsConstructor
public class ReleaseBundleIModel extends BaseModel {

    String name;
    String version;
    String desc;
    String created;
    long size;
    long downloads;
    String signature;
    String status;
    List<ReleaseArtifactIModel> artifacts = Lists.newArrayList();
}
