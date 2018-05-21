package org.artifactory.addon.go;

import lombok.Data;

import java.util.List;

/**
 * @author Liz Dashevski
 */
@Data
public class GoMetadataInfo {

    private GoInfo goInfo;
    private List<GoDependency> goDependencies;

    GoMetadataInfo() {
    }

    GoMetadataInfo(GoInfo npmInfo, List<GoDependency> dependencies) {
        this.goInfo = npmInfo;
        this.goDependencies = dependencies;
    }
}
