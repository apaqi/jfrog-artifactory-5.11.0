package org.artifactory.storage.db.bundle.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseBundleModel;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static org.jfrog.common.ArgUtils.*;

@Data
@NoArgsConstructor
public class ArtifactsBundle {
    long id;
    String name;
    String version;
    BundleTransactionStatus status;
    DateTime dateCreated;
    String signature;

    private static final Pattern pattern = Pattern.compile("^[a-zA-Z\\d]+[a-zA-Z\\d\\-_:.]*$");
    private static final String INVALID_PATTERN_MESSAGE = " can only contain letters, numbers and the following characters: .-_:";

    public static ArtifactsBundle buildFrom(@Nonnull ReleaseBundleModel releaseBundleModel) {
        ArtifactsBundle artifactsBundle = new ArtifactsBundle();
        artifactsBundle.setName(releaseBundleModel.getName());
        artifactsBundle.setVersion(releaseBundleModel.getVersion());
        artifactsBundle.setDateCreated(parseDateCreated(releaseBundleModel.getCreated()));
        artifactsBundle.setStatus(BundleTransactionStatus.INPROGRESS);
        artifactsBundle.setSignature(releaseBundleModel.getSignature());
        artifactsBundle.validate();
        return artifactsBundle;

    }

    private static DateTime parseDateCreated(@Nonnull String date) {
        return ISODateTimeFormat.dateTime().withZoneUTC().parseDateTime(date);
    }

    public void validate() {
        validate(name, version, dateCreated, signature);
    }

    public void validate(String name, String version, DateTime dateCreated, String signature) {
        this.name = requireMatches(name, pattern, "Bundle name" + INVALID_PATTERN_MESSAGE);
        this.version = requireMatches(version, pattern, "Bundle version" + INVALID_PATTERN_MESSAGE);
        this.dateCreated = requireNonNull(dateCreated, "Date created must not be null");
        this.signature = requireNonBlank(signature, "Signature is mandatory");
    }
}
