package org.artifactory.ui.rest.service.admin.security.openid;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AuthenticationHelper;
import org.artifactory.security.SingleSignOnService;
import org.jfrog.access.token.JwtAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Noam Shemesh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RedirectService implements RestService<String> {

    private static final Logger log = LoggerFactory.getLogger(RedirectService.class);

    private final SingleSignOnService singleSignOnService;

    @Autowired
    public RedirectService(SingleSignOnService singleSignOnService) {
        this.singleSignOnService = singleSignOnService;
    }

    @Override
    public void execute(ArtifactoryRestRequest<String> request, RestResponse response) {
        Authentication authentication = AuthenticationHelper.getAuthentication();
        if (authentication == null) {
            response.error("User is logged out");
            return;
        }

        Optional<JwtAccessToken> jwtAccessToken = singleSignOnService.extractAndVerifyToken(request.getImodel());
        if (!jwtAccessToken.isPresent()) {
            response.error("Cannot redirect to requested service, access token could not be extracted and verified from request");
            return;
        }

        JwtAccessToken tokenValue = jwtAccessToken.get();
        if (tokenValue.getAudience().size() != 1) {
            response.error("Token audience is unexpected");
            return;
        }

        String username = authentication.getPrincipal().toString();
        String url = singleSignOnService.getRedirectTargetUrlWithToken(username,
                singleSignOnService.extractAuthenticatedUserInfo(username, authentication),
                singleSignOnService.extractRedirectUrlFromToken(tokenValue).orElseThrow(() -> getException("redirect url")),
                singleSignOnService.extractExtraOpenidParameters(tokenValue));

        log.debug("Redirecting user to {}", url);

        response.iModel(RedirectResponse.builder().url(url).build());
    }

    private IllegalArgumentException getException(String key) {
        return new IllegalArgumentException("Missing " + key + " in token payload");
    }
}
