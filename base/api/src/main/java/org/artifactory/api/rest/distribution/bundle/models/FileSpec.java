package org.artifactory.api.rest.distribution.bundle.models;

import com.google.common.base.Joiner;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jfrog.common.ArgUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class FileSpec {

    @JsonProperty(value = "source_path")
    String sourcePath;
    List<ArtifactProperty> props;
    @JsonProperty(value = "target_artifactory_url")
    String targetArtifactoryUrl;
    @JsonProperty(value = "target_path")
    String targetPath;
    @JsonProperty(value = "release_bundle")
    boolean releaseBundle;
    @JsonIgnore
    String internalTmpPath;

    public void setInternalTmpPath(String transactionPath) {
        this.internalTmpPath = Joiner.on("/")
                .join(transactionPath, targetPath);
    }

    public void validate(){
       sourcePath = ArgUtils.requireNonBlank(sourcePath,"source_path must not be blank");
       targetPath = ArgUtils.requireNonBlank(targetPath,"target_path must not be blank");
       targetArtifactoryUrl = ArgUtils.requireNonBlank(targetArtifactoryUrl,"target_artifactory_url must not be blank");
    }
}
