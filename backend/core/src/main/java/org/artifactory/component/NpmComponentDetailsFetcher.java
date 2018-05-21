package org.artifactory.component;

import org.artifactory.api.component.ComponentDetails;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.jfrog.client.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.artifactory.mime.NamingUtils.getMimeType;

@Component("npmComponentDetailsFetcher")
public class NpmComponentDetailsFetcher implements RepoComponentDetailsFetcher {

    @Autowired
    PropertiesService propertiesService;

    @Override
    public ComponentDetails calcComponentDetails(RepoPath repoPath) {
        Properties properties = propertiesService.getProperties(repoPath);
        return new ComponentDetails()
                .componentType(RepoType.Npm)
                .name(properties.getFirst("npm.name"))
                .version(properties.getFirst("npm.version"))
                .extension(PathUtils.getExtension(repoPath.getName()))
                .mimeType(getMimeType(repoPath.getPath()).getType());
    }
}
