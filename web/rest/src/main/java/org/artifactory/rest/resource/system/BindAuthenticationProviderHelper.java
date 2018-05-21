package org.artifactory.rest.resource.system;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.access.CreatedTokenInfo;
import org.artifactory.api.security.access.TokenSpec;
import org.artifactory.api.security.access.UserTokenSpec;
import org.artifactory.rest.resource.token.TokenResponseModel;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.common.ServiceType;
import org.jfrog.security.file.PemHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class BindAuthenticationProviderHelper {

    public void validateUserInput(AuthenticationProviderInfo authenticationProviderInfo) {
        if (authenticationProviderInfo == null || StringUtils.isBlank(authenticationProviderInfo.getOwnerServiceId())) {
            throw new IllegalArgumentException("Invalid value for owner_service_id attribute, Cannot be null");
        }
        ServiceId serviceId = ServiceId.fromFormattedName(authenticationProviderInfo.getOwnerServiceId());
        if (StringUtils.isBlank(serviceId.getInstanceId()) || StringUtils.isBlank(serviceId.getServiceType())) {
            throw new IllegalArgumentException("Invalid value for owner_service_id attribute");
        }
        if (authenticationProviderInfo.getType() == null ||
                (!authenticationProviderInfo.getType().equals(ServiceType.ARTIFACTORY) &&
                        !authenticationProviderInfo.getType().equals(ServiceType.ACCESS))) {
            throw new IllegalArgumentException(
                    "Invalid value for type attribute, expected " + ServiceType.ARTIFACTORY + "/" + ServiceType.ACCESS);
        }
        try {
            new URL(authenticationProviderInfo.getUrl());
            PemHelper.readCertificate(authenticationProviderInfo.getCertificate());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid value for url attribute");
        } catch (Exception e2) {
            throw new IllegalArgumentException("Invalid value for certificate attribute");
        }
    }

    public CreatedTokenInfo createToken(String ownerServiceId) {
        AccessService accessService = ContextHelper.get().beanForType(AccessService.class);
        ServiceId artifactoryServiceId = accessService.getArtifactoryServiceId();
        TokenSpec tokenSpec = UserTokenSpec.create(ownerServiceId)
                .scope(Arrays.asList(artifactoryServiceId.getFormattedName() + ":admin"))
                .expiresIn(0L)
                .refreshable(true)
                .audience(Arrays.asList(artifactoryServiceId.getFormattedName()));
        return accessService.createToken(tokenSpec);
    }

    public TokenResponseModel toTokenResponseModel(CreatedTokenInfo createdTokenInfo) {
        return TokenResponseModel.builder().expiresIn(createdTokenInfo.getExpiresIn())
                .refreshToken(createdTokenInfo.getRefreshToken())
                .scope(createdTokenInfo.getScope())
                .accessToken(createdTokenInfo.getTokenValue())
                .tokenType(createdTokenInfo.getTokenType()).build();
    }
}
