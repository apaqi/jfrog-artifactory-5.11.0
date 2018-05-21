/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.rest.resource.system;


import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.access.CreatedTokenInfo;
import org.artifactory.descriptor.security.EncryptionPolicy;
import org.artifactory.descriptor.security.PasswordSettings;
import org.artifactory.security.SecurityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author freds
 */
public class SecurityResource {
    private static final Logger log = LoggerFactory.getLogger(SecurityResource.class);
    private final AddonsManager addonManager;

    private SecurityService securityService;

    private CentralConfigService centralConfigService;
    private HttpServletRequest httpServletRequest;


    public SecurityResource(SecurityService securityService, CentralConfigService service,
            HttpServletRequest httpServletRequest, AddonsManager addonsManager) {
        this.securityService = securityService;
        centralConfigService = service;
        this.httpServletRequest = httpServletRequest;
        this.addonManager = addonsManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public SecurityInfo getSecurityData() {
        log.warn("Security XML export is deprecated");
        return securityService.getSecurityData();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    @Deprecated
    public String importSecurityData(String securityXml) {
        log.warn("Security XML import is deprecated");
        log.debug("Activating import of new security data: {}", securityXml);
        securityService.importSecurityData(securityXml);
        SecurityInfo securityData = securityService.getSecurityData();
        int x = securityData.getUsers().size();
        int y = securityData.getGroups().size();
        int z = securityData.getAcls().size();
        return "Import of new Security data (" + x + " users, " + y + " groups, " + z + " acls) succeeded";
    }

    @POST
    @Path("passwordPolicy/{policyName}")
    public void setPasswordPolicy(@PathParam("policyName") String policyName) {
        EncryptionPolicy policy;
        try {
            policy = EncryptionPolicy.valueOf(policyName);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        PasswordSettings passwordSettings = centralConfigService.getDescriptor().getSecurity().getPasswordSettings();
        passwordSettings.setEncryptionPolicy(policy);
    }

    @POST
    @Path("logout")
    public void logout() {
        httpServletRequest.getSession().invalidate();
    }

    @Path(SystemRestConstants.PATH_CERTIFICATES)
    public CertificatesResource getSecurityResource() {
        return new CertificatesResource(addonManager);
    }

    /**
     * Binding a service to authentication provider.
     * Accept all authentication provider information: url, token & certificate.
     *
     * @param authenticationProviderInfo - contains information about the service to be bound with
     * @return bindAuthenticationResult - contains access admin token
     */
    @POST
    @Path("access")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response bindAuthenticationProvider(AuthenticationProviderInfo authenticationProviderInfo) {
        BindAuthenticationResult bindAuthenticationResult = new BindAuthenticationResult();
        BindAuthenticationProviderHelper bindAuthenticationProviderHelper = new BindAuthenticationProviderHelper();
        try {
            bindAuthenticationProviderHelper.validateUserInput(authenticationProviderInfo);
            CreatedTokenInfo createdTokenInfo = bindAuthenticationProviderHelper
                    .createToken(authenticationProviderInfo.getOwnerServiceId());
            bindAuthenticationResult
                    .setStatus(new BindAuthenticationStatusResult(0, "Success"));
            bindAuthenticationResult.setToken(bindAuthenticationProviderHelper.toTokenResponseModel(createdTokenInfo));
            return Response.status(Response.Status.OK).entity(bindAuthenticationResult).build();
        } catch (IllegalArgumentException e) {
            bindAuthenticationResult.setStatus(
                    new BindAuthenticationStatusResult(4,
                            "Fail to execute, Reason: " + e.getMessage()));
            log.debug("Error while trying to authenticate provider, Reason: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(bindAuthenticationResult).build();
        } catch (Exception e) {
            bindAuthenticationResult.setStatus(
                    new BindAuthenticationStatusResult(5,
                            "Fail to execute, reason: " + e.getMessage()));
            log.error("Error while trying to authenticate provider, Reason: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(bindAuthenticationResult).build();
        }
    }


}
