package org.artifactory.rest.resource.binary.provider;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.binary.provider.BinaryProviderApiAddon;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path(SystemRestConstants.PATH_BINARY_SERVICES)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component
public class BinaryServicesResource {

    @Autowired
    private AddonsManager addonsManager;


    @Path("file/{sha256}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    @GET
    public Response getBinaryBySha256(@PathParam("sha256") String sha256) {
        BinaryProviderApiAddon addon = addonsManager.addonByType(BinaryProviderApiAddon.class);
        InputStream stream = addon.getBinaryBySha256(sha256);
        return Response.ok(stream).build();
    }

    @Path("part/{sha256}/{start}/{end}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    @GET
    public Response getBinaryPartBySha256(@PathParam("sha256") String sha256, @PathParam("start") long start,
            @PathParam("end") long end) {
        BinaryProviderApiAddon addon = addonsManager.addonByType(BinaryProviderApiAddon.class);
        InputStream stream = addon.getBinaryPartBySha256(sha256, start, end);
        return Response.ok(stream).build();
    }

}
