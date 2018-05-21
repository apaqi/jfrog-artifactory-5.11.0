package org.artifactory.rest.resource.system;


import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.rest.services.SystemTopologyResourceHelper;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource to get information about Artifactory services.
 */

@Component
@Path(SystemRestConstants.SYSTEM_TOPOLOGY)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
public class SystemTopologyResource {
    private static final Logger log = LoggerFactory.getLogger(SystemTopologyResource.class);

    @Autowired
    private SystemTopologyResourceHelper systemTopologyResourceHelper;

    @Context
    private HttpServletRequest servletRequest;

    @GET
    @Path(SystemRestConstants.PATH_INFO)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceInfo() {
        try {
            String artifactoryUrl = HttpUtils.getServletContextUrl(servletRequest);
            EnterprisePlusServiceInfo enterprisePlusServiceInfo = systemTopologyResourceHelper
                    .createEnterprisePlusServiceInfo(artifactoryUrl);
            SystemServiceInfo systemServiceInfo = systemTopologyResourceHelper
                    .createSystemServiceInfo(enterprisePlusServiceInfo, artifactoryUrl);
            return Response.ok().entity(systemServiceInfo).build();
        } catch (Exception e) {
            String msg = "Could not retrieve service info";
            log.debug("Could not retrieve service info, due to: " + e.getMessage());
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            e.getMessage()))
                    .build();
        }
    }
}
