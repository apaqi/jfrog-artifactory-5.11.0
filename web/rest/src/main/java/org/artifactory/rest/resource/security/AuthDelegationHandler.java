package org.artifactory.rest.resource.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.dataholder.UserWithGroupsWrapper;
import org.artifactory.security.HttpAuthenticationDetails;
import org.artifactory.security.SingleSignOnService;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.client.token.TokenVerifyResult;
import org.jfrog.access.rest.user.UserWithGroups;
import org.jfrog.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * Hanlde request login by external service.
 * Supports basic auth and
 *
 * @author Tamir Hadad
 */
public class AuthDelegationHandler {

    private static final String ERROR_GET_USER_DETAILS = "{\"error\" : \"Couldn't get user details\"}";
    private final HttpServletRequest request;
    private final JsonUtils mapper = createMapper();
    private static final Logger log = LoggerFactory.getLogger(AuthDelegationHandler.class);

    private static JsonUtils createMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return JsonUtils.createInstance(objectMapper);
    }

    public AuthDelegationHandler(HttpServletRequest request) {
        this.request = request;
    }

    public Response handleRequest(SingleSignOnService singleSignOnService, UserAuthDetails userAuthDetails) {
        AccessService accessService = ContextHelper.get().beanForType(AccessService.class);
        String token = userAuthDetails.getToken();
        if (StringUtils.isEmpty(token)) {
            return handleBasicAuth(singleSignOnService, userAuthDetails);
        } else {
            return handleToken(singleSignOnService, token, accessService);
        }
    }

    private Response handleToken(SingleSignOnService singleSignOnService, String token, AccessService accessService) {
        TokenVerifyResult verify = accessService.getAccessClient().token().verify(token);
        if (verify.isSuccessful()) {
            String userWithGroupsWrapper = accessService.parseToken(token).getExtension();
            if (StringUtils.isNotEmpty(userWithGroupsWrapper)) {
                UserWithGroups tokenUserWithGroupData =
                        mapper.readValue(userWithGroupsWrapper, UserWithGroupsWrapper.class).getUsr();
                return Response.ok(singleSignOnService.convertUserToJson(tokenUserWithGroupData)).build();
            }
        } else {
            String erroMsg = "{\"error\" : \"Verify reason:, " + verify.getReason() + "\"}";
            log.error("Token verification failed due to:" + verify.getReason());
            return Response.status(Response.Status.FORBIDDEN).entity(erroMsg).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ERROR_GET_USER_DETAILS).build();
    }

    private Response handleBasicAuth(SingleSignOnService singleSignOnService, UserAuthDetails userAuthDetails) {
        Authentication authentication;
        // Basic Authentication
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userAuthDetails.getUsername().toLowerCase(),
                        userAuthDetails.getPassword());
        // Throws exeption in case authentication fails
        authentication = authenticateCredentials(authenticationToken);
        UserWithGroups userWithGroups = singleSignOnService.findUserWithGroups(userAuthDetails.getUsername(), authentication);
        String userWithGroupJson = singleSignOnService.convertUserToJson(userWithGroups);
        return Response.ok().entity(userWithGroupJson).build();
    }

    /**
     * authenticate credential against Security providers (Artifactory,Ldap , crown and etc)
     *
     * @param authenticationToken - user credentials
     * @return Authentication Data
     */
    private Authentication authenticateCredentials(UsernamePasswordAuthenticationToken authenticationToken) {
        AuthenticationManager authenticationManager = ContextHelper.get().beanForType(AuthenticationManager.class);
        HttpAuthenticationDetails details = new HttpAuthenticationDetails(request);
        authenticationToken.setDetails(details);
        return authenticationManager.authenticate(authenticationToken);
    }
}