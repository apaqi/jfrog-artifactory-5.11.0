package org.artifactory.ui.rest.service.admin.security.keys;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.keys.KeysAddon;
import org.artifactory.keys.TrustedKey;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ValidateTrustedKeyService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(ValidateTrustedKeyService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String keyAlias = request.getPathParamByKey("key_alias");

        KeysAddon addon = addonsManager.addonByType(KeysAddon.class);

        Optional<TrustedKey> foundedKey = addon.findTrustedKeyByAlias(keyAlias);

        if (foundedKey.isPresent()) {
            log.error("Key alias is already exist");
            setError(response);
        } else {
            response.responseCode(HttpStatus.OK.value());
        }
    }

    private void setError(RestResponse response) {
        response.responseCode(HttpStatus.BAD_REQUEST.value());
        response.error("Key alias is already exist");
    }
}
