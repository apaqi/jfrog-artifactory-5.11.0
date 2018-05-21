package org.artifactory.security;

import org.artifactory.api.security.access.CreatedTokenInfo;
import org.jfrog.access.rest.user.UserWithGroups;
import org.jfrog.access.token.JwtAccessToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

/**
 * @author Noam Shemesh
 */
public interface SingleSignOnService {
    String createAccessTokenForUri(String callerServiceId, String serviceUrl, String subject);

    Optional<JwtAccessToken> extractAndVerifyToken(String redirectUrl);

    String convertUserToJson(UserWithGroups tokenUserWithGroupData);

    String getRedirectTargetUrlWithToken(String username, UserInfo userInfo, String redirectUrl,
                                         Map<String, String> extraArgs);

    UserInfo extractAuthenticatedUserInfo(String username, Authentication authentication);

    UserWithGroups findUserWithGroups(String username, Authentication authentication);

    Optional<String> extractRedirectUrlFromToken(JwtAccessToken jwtAccessToken);

    Optional<String> extractServiceIdFromToken(JwtAccessToken jwtAccessToken);

    Map<String, String> extractExtraOpenidParameters(JwtAccessToken jwtAccessToken);

    CreatedTokenInfo verifyTokenAndChangeAudience(String accessToken);

    CreatedTokenInfo verifyTokenAndAppendStateParamToToken(String serviceId, String state, String redirectUri);
}
