package org.artifactory.security.access.emigrate.conveter;

import org.apache.commons.lang.mutable.MutableInt;
import org.artifactory.security.access.AccessService;
import org.artifactory.security.access.emigrate.AccessConverter;
import org.jfrog.access.common.ResourceType;
import org.jfrog.access.rest.permission.UpdatePermissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Noam Shemesh
 */
@Component
public class V5100ConvertResourceTypeToRepo implements AccessConverter {
    private static final Logger log = LoggerFactory.getLogger(V5100ConvertResourceTypeToRepo.class);

    private final AccessService accessService;

    @Autowired
    public V5100ConvertResourceTypeToRepo(AccessService accessService) {
        this.accessService = accessService;
    }

    @Override
    public void convert() {
        log.info("Starting '5.10: Add resource type to permission target' Access Conversion");
        MutableInt count = new MutableInt();
        accessService.getAccessClient().permissions()
                .findPermissionsByServiceId(accessService.getArtifactoryServiceId())
                .getPermissions()
                .stream()
                .filter(permission -> ResourceType.SERVICE.equals(permission.getResourceType()))
                .forEach(permission -> {
                    accessService.getAccessClient().permissions()
                            .updatePermission((UpdatePermissionRequest)
                                    UpdatePermissionRequest.create()
                                            .name(permission.getName())
                                            .resourceType(ResourceType.REPO));
                    count.increment();
                });

        log.info("Finished '5.10: Add resource type to permission target' Access Conversion. Updated {} permission targets.", count);
    }
}
