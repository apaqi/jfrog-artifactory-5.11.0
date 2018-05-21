package org.artifactory.security.access.emigrate.conveter;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.security.access.AccessService;
import org.artifactory.security.access.emigrate.AccessConverter;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.security.service.access.UserMapper;
import org.jfrog.access.rest.user.UpdateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper.getSearchableProp;

/**
 * Updating all users with decrypted custom data, and let Access encrypt whatever is sensitive
 * @author Noam Shemesh
 */
@Component
public class V5100DecryptAllUsersCustomData implements AccessConverter {

    private static final Logger log = LoggerFactory.getLogger(V5100DecryptAllUsersCustomData.class);

    private final AccessService accessService;

    @Autowired
    public V5100DecryptAllUsersCustomData(AccessService accessService) {
        this.accessService = accessService;
    }

    @Override
    public void convert() {
        log.info("Start decrypting all users custom data");
        decryptUserProps();
        log.info("Finished decrypting all users custom data");
    }

    private void decryptUserProps() {
        try {
            accessService.getAccessClient().users().findUsers().getUsers().forEach(user -> {
                MutableBoolean updated = new MutableBoolean(false);
                UpdateUserRequest updateRequest = UpdateUserRequest.create();

                user.getCustomData().forEach((propKey, propValue) -> {
                    String newValue = CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), propValue);
                    if (UserMapper.isFieldSensitive(propKey) || !newValue.equals(propValue)) {
                        log.debug("Decrypting user {} property {}", user.getUsername(), propKey);

                        updateRequest.username(user.getUsername()).addCustomData(propKey, newValue,
                                UserMapper.isFieldSensitive(propKey));
                        addSearchableField(updateRequest, propKey, newValue);

                        updated.setTrue();
                    }
                });

                if (updated.booleanValue()) {
                    accessService.getAccessClient().users().updateUser(updateRequest);
                }
            });
        } catch (Exception e) {
            log.error("Could not decrypt user props, cause: {}", e);
            throw new StorageException("Could not decrypt user props, see logs for more details");
        }
    }

    private void addSearchableField(UpdateUserRequest updateRequest, String propKey, String newValue) {
        Pair<String, String> searchableProp = getSearchableProp(propKey, newValue);
        if (searchableProp != null) {
            updateRequest.addCustomData(searchableProp.getKey(), searchableProp.getValue(), false);
        }
    }
}
