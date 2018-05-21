package org.artifactory.security.access.emigrate;

import org.artifactory.environment.converter.local.version.v5.V5100AddAccessDecryptAllUsersMarkerFile;
import org.artifactory.security.access.emigrate.conveter.AccessSecurityEmigratorImpl;
import org.artifactory.security.access.emigrate.conveter.V5100ConvertResourceTypeToRepo;
import org.artifactory.security.access.emigrate.conveter.V5100DecryptAllUsersCustomData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Noam Shemesh
 */
@Component
public class AccessConverters {
    private final AccessSecurityEmigratorImpl accessSecurityEmigrator;
    private final V5100ConvertResourceTypeToRepo resourceTypeConverter;
    private final V5100DecryptAllUsersCustomData decryptAllUsersCustomData;

    @Autowired
    public AccessConverters(AccessSecurityEmigratorImpl accessSecurityEmigrator,
                            V5100ConvertResourceTypeToRepo resourceTypeConverter,
                            V5100DecryptAllUsersCustomData decryptAllUsersCustomData) {
        this.accessSecurityEmigrator = accessSecurityEmigrator;
        this.resourceTypeConverter = resourceTypeConverter;
        this.decryptAllUsersCustomData = decryptAllUsersCustomData;
    }

    public AccessConverter getSecurityEmigrator() {
        return accessSecurityEmigrator;
    }

    public AccessConverter getResourceTypeConverter() {
        return resourceTypeConverter;
    }

    public AccessConverter getUserCustomDataDecryptionConverter() {
        return decryptAllUsersCustomData;
    }
}
