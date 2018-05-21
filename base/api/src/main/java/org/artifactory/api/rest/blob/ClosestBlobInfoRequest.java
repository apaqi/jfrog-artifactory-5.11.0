package org.artifactory.api.rest.blob;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.mime.MimeType;
import org.codehaus.jackson.annotate.JsonProperty;

import static org.jfrog.common.ArgUtils.requireNonBlank;

/**
 * @author Rotem Kfir
 */
@ToString
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ClosestBlobInfoRequest {

    @JsonProperty
    private String checksum; // sha256

    @JsonProperty("component_type")
    private RepoType componentType;

    @JsonProperty("component_name")
    private String componentName;

    @JsonProperty("component_version")
    private String componentVersion;

    @JsonProperty("component_mimetype")
    private String componentMimetype;

    @JsonProperty("artifact_name")
    private String artifactName;

    private ClosestBlobInfoRequest(String checksum, String mimeType, RepoType componentType, String componentName, String componentVersion,
            String artifactName) {
        this.checksum = requireNonBlank(checksum, "Checksum is required");
        this.componentType = componentType;
        this.componentName = componentName;
        this.componentVersion = componentVersion;
        this.artifactName = requireNonBlank(artifactName, "Artifact name is required");
        this.componentMimetype = mimeType == null || mimeType.isEmpty() ? MimeType.def.getType() : mimeType;
    }

    public void validate() {
        requireNonBlank(checksum, "Checksum is required");
        requireNonBlank(artifactName, "Artifact name is required");
        requireNonBlank(componentMimetype, "Component Mime Type is required");
    }

    public String getChecksum() {
        return checksum;
    }

    public RepoType getComponentType() {
        return componentType;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersion() {
        return componentVersion;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public String getComponentMimetype() { return componentMimetype; }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String checksum;
        private RepoType componentType;
        private String componentName;
        private String componentVersion;
        private String componentMimetype;
        private String artifactName;

        public Builder() {
        }

        public Builder setChecksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder setComponentType(RepoType componentType) {
            this.componentType = componentType;
            return this;
        }

        public Builder setComponentName(String componentName) {
            this.componentName = componentName;
            return this;
        }

        public Builder setComponentVersion(String componentVersion) {
            this.componentVersion = componentVersion;
            return this;
        }

        public Builder setArtifactName(String artifactName) {
            this.artifactName = artifactName;
            return this;
        }

        public Builder setComponentMimetype(String mimeType){
            this.componentMimetype = mimeType;
            return this;
        }

        public ClosestBlobInfoRequest build() {
            return new ClosestBlobInfoRequest(checksum, componentMimetype, componentType, componentName, componentVersion, artifactName);
        }
    }
}
