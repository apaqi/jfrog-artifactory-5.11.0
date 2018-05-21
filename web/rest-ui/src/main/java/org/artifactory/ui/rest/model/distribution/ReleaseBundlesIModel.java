package org.artifactory.ui.rest.model.distribution;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

@Data
@NoArgsConstructor
public class ReleaseBundlesIModel extends BaseModel {

    List<BundleSummeryModel> bundles = Lists.newArrayList();
}
