package org.artifactory.api.component;

import lombok.Data;
import lombok.experimental.Accessors;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.MimeType;

@Data
@Accessors(fluent = true)
public class ComponentDetails {
    private String name;
    private String version;
    private RepoType componentType;
    private String extension;
    private String mimeType;

    public ComponentDetails() {
        mimeType = MimeType.def.getType();
    }
}
