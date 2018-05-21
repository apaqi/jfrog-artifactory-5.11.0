package org.artifactory.component;

import org.artifactory.api.component.ComponentDetails;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.jfrog.client.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.artifactory.mime.NamingUtils.getMimeType;

@Component("dockerComponentDetailsFetcher")
public class DockerComponentDetailsFetcher implements RepoComponentDetailsFetcher {
    private static final String MANIFEST = "manifest.json";

    @Autowired
    PropertiesService propertiesService;

    @Override
    public ComponentDetails calcComponentDetails(RepoPath repoPath) {
        RepoPath manifestRepoPath = repoPath;
        if (!MANIFEST.equals(repoPath.getName())) {
            if (repoPath.getParent() == null) {
                return new ComponentDetails();
            }
            manifestRepoPath = new RepoPathImpl(repoPath.getParent(), MANIFEST);
        }
        Properties properties = propertiesService.getProperties(manifestRepoPath);
        return new ComponentDetails()
                .componentType(RepoType.Docker)
                .name(properties.getFirst("docker.repoName"))
                .version(properties.getFirst("docker.manifest"))
                .extension(PathUtils.getExtension(repoPath.getName()))
                .mimeType(getMimeType(repoPath.getPath()).getType());
    }
}
