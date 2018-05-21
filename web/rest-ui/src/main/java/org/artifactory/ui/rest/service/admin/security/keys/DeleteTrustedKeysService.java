package org.artifactory.ui.rest.service.admin.security.keys;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.keys.KeysAddon;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.keys.KidIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteTrustedKeysService implements RestService  {

    private static final Logger log = LoggerFactory.getLogger(DeleteTrustedKeysService.class);

    @Autowired
    AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<KidIModel> kidsToDelete = (List<KidIModel>)request.getModels();

        KeysAddon addon = addonsManager.addonByType(KeysAddon.class);

        if (kidsToDelete != null) {
            kidsToDelete.forEach(kid -> addon.deleteTrustedKey(kid.getKid()));
        }

        log.debug("All requested trusted keys were deleted successfully");

        response.info("All requested trusted keys were deleted successfully")
                .responseCode(HttpStatus.NO_CONTENT.value());
    }
}
