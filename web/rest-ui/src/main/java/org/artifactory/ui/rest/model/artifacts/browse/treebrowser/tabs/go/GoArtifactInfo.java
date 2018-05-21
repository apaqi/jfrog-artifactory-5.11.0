package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.go;

import org.artifactory.addon.go.GoDependency;
import org.artifactory.addon.go.GoInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

import java.util.List;

/**
 * @author Liz Dashevski
 */
public class GoArtifactInfo extends BaseArtifactInfo {

    private GoInfo goInfo;
    private List<GoDependency> goDependencyList;

    @SuppressWarnings({"UnusedDeclaration"})
    public GoInfo getGoInfo() {
        return goInfo;
    }

    public void setGoInfo(GoInfo goInfo) {
        this.goInfo = goInfo;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public List<GoDependency> getGoDependencies() {
        return goDependencyList;
    }

    public void setGoDependencies(List<GoDependency> goDependencyList) {
        this.goDependencyList = goDependencyList;
    }
}
