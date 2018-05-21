package org.artifactory.component;

import org.artifactory.api.component.ComponentDetails;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.artifactory.api.module.ModuleInfoUtils.moduleInfoFromArtifactPath;
import static org.artifactory.api.module.ModuleInfoUtils.moduleInfoFromDescriptorPath;
import static org.artifactory.mime.NamingUtils.getMimeType;

@Component("mavenComponentDetailsFetcher")
public class MavenComponentDetailsFetcher implements RepoComponentDetailsFetcher {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public ComponentDetails calcComponentDetails(RepoPath repoPath) {

        String path = repoPath.getPath();
        ModuleInfo moduleInfo;
        RepoDescriptor repo = repositoryService.repoDescriptorByKey(repoPath.getRepoKey());
        if (repo != null) {
            RepoLayout repoLayout = repo.getRepoLayout();
            moduleInfo = moduleInfoFromDescriptorPath(path, repoLayout);
            if (!moduleInfo.isValid()) {
                moduleInfo = moduleInfoFromArtifactPath(path, repoLayout);
            }
        } else {
            moduleInfo = new ModuleInfo();
        }
        String componentName = moduleInfo.getOrganization() == null ? null : moduleInfo.getOrganization() + ":" + moduleInfo.getModule();
        return new ComponentDetails()
                .componentType(RepoType.Maven)
                .name(componentName)
                .version(moduleInfo.getBaseRevision())
                .extension(moduleInfo.getExt())
                .mimeType(getMimeType(path).getType());
    }
}
