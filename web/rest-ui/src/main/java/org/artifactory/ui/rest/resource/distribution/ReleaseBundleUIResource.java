package org.artifactory.ui.rest.resource.distribution;


import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.service.distribution.ReleaseBundleServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.artifactory.api.rest.constant.SystemRestConstants.JFROG_RELEASE;

@Path("bundles")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReleaseBundleUIResource extends BaseResource {

    @Autowired
    ReleaseBundleServiceFactory releaseBundleService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getBundles() {
        return runService(releaseBundleService.getAllBundles());
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getBundleVersions() {
        return runService(releaseBundleService.getAllBundleVersions());
    }

    @GET
    @Path("{name}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getBundle() {
        return runService(releaseBundleService.getReleaseBundle());
    }

    @DELETE
    @Path("{name}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response deleteBundle(@QueryParam("include_content") Boolean includeContent) {
        return runService(releaseBundleService.deleteReleaseBundle());
    }


}
