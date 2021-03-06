/*
 *
 * Copyright 2016 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.logging.sumo;

import com.google.common.collect.ImmutableSet;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.security.props.auth.EncryptedTokenManager;
import org.artifactory.util.MaskedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.artifactory.common.crypto.CryptoHelper.decryptIfNeeded;

/**
 * <p>Created on 19/07/16
 *
 * @author Yinon Avraham
 */
@Component
public class SumoLogicTokenManagerImpl implements SumoLogicTokenManager, EncryptedTokenManager {
    private static final Logger log = LoggerFactory.getLogger(SumoLogicTokenManagerImpl.class);

    public static final String PROP_KEY_SUMO_REFRESH_TOKEN = "sumologic.refresh.token";
    public static final String PROP_KEY_SUMO_ACCESS_TOKEN = "sumologic.access.token";
    private static final Set<String> propKeys = ImmutableSet.of(PROP_KEY_SUMO_REFRESH_TOKEN, PROP_KEY_SUMO_ACCESS_TOKEN);


    @Autowired
    private UserGroupService userGroupService;

    @Override
    public String getRefreshToken(String username) {
        log.trace("Getting Sumo Logic refresh token for user {}", username);
        String token = userGroupService.getUserProperty(username, PROP_KEY_SUMO_REFRESH_TOKEN);
        return decryptIfNeeded(ArtifactoryHome.get(), token);
    }

    @Override
    public String getAccessToken(String username) {
        log.trace("Getting Sumo Logic access token for user {}", username);
        String token = userGroupService.getUserProperty(username, PROP_KEY_SUMO_ACCESS_TOKEN);
        return decryptIfNeeded(ArtifactoryHome.get(), token);
    }

    @Override
    public void updateTokens(String username, String refreshToken, String accessToken) {
        log.trace("Updating Sumo Logic access tokens for user {} to: refresh={}, access={}", username,
                MaskedValue.of(refreshToken), MaskedValue.of(accessToken));
        userGroupService.addUserProperty(username, PROP_KEY_SUMO_REFRESH_TOKEN, refreshToken);
        userGroupService.addUserProperty(username, PROP_KEY_SUMO_ACCESS_TOKEN, accessToken);
    }

    @Override
    public void updateAccessToken(String username, String accessToken) {
        log.trace("Updating Sumo Logic access token for user {} to {}", username, MaskedValue.of(accessToken));
        userGroupService.addUserProperty(username, PROP_KEY_SUMO_ACCESS_TOKEN, accessToken);
    }

    @Override
    public void revokeTokens(String username) {
        log.trace("Revoking Sumo Logic tokens for user {}", username);
        userGroupService.deleteUserProperty(username, PROP_KEY_SUMO_REFRESH_TOKEN);
        userGroupService.deleteUserProperty(username, PROP_KEY_SUMO_ACCESS_TOKEN);
    }

    @Override
    public void revokeAccessToken(String username) {
        log.trace("Revoking Sumo Logic access token for user {}", username);
        userGroupService.deleteUserProperty(username, PROP_KEY_SUMO_ACCESS_TOKEN);
    }

    @Override
    public void revokeAllTokens() {
        log.trace("Revoking Sumo Logic tokens for all users");
        userGroupService.deletePropertyFromAllUsers(PROP_KEY_SUMO_REFRESH_TOKEN);
        userGroupService.deletePropertyFromAllUsers(PROP_KEY_SUMO_ACCESS_TOKEN);
    }

    @Override
    public Set<String> getPropKeys() {
        return propKeys;
    }
}
