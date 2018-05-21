package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.recalculateindex;

import org.artifactory.addon.helm.HelmAddon;
import org.artifactory.descriptor.repo.RepoType;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Yuval Reches
 */
@JsonTypeName("Helm")
public class HelmIndexCalculator extends BaseIndexCalculator {

    @Override
    public void calculateIndex() throws Exception {
        HelmAddon helmAddon = addonsManager.addonByType(HelmAddon.class);
        String repoKey = getRepoKey();
        if (helmAddon != null && assertRepoType(repoKey, RepoType.Helm)) {
            helmAddon.requestReindexRepo(repoKey, true);
        }
    }
}
