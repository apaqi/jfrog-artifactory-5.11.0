package org.artifactory.rest.resource.replicator;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replicator.ReplicatorAddon;
import org.artifactory.addon.replicator.ReplicatorDetails;
import org.artifactory.addon.replicator.ReplicatorRegistrationRequest;
import org.artifactory.api.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.artifactory.api.rest.constant.ReplicatorRestConstants.*;

@Component
@Path(PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class ReplicatorResource {

    @Autowired
    private AddonsManager addonsManager;

    @GET
    @Path(PATH_LOCAL_DETAILS)
    @Produces({MediaType.APPLICATION_JSON})
    public Response getLocalReplicatorDetails() {
        ReplicatorAddon replicatorAddon = addonsManager.addonByType(ReplicatorAddon.class);
        String externalUrl = replicatorAddon.getExternalUrl();
        if (externalUrl != null) {
            ReplicatorDetailsResponse res = ReplicatorDetailsResponse.builder()
                    .externalUrl(externalUrl)
                    .build();
            return Response.ok(res).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("No replicator is registered")
                .build();
    }

    @GET
    @Path(PATH_AUTHORIZED)
    @Produces({MediaType.APPLICATION_JSON})
    public Response isAuthorized() {
        return Response.ok().build();
    }

    @POST
    @Path(PATH_REGISTER)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public Response register(ReplicatorRegistrationRequest registrationRequest) {
        ReplicatorAddon replicatorAddon = addonsManager.addonByType(ReplicatorAddon.class);
        try {
            ReplicatorDetails replicatorDetails = replicatorAddon.register(registrationRequest);
            return Response.ok(replicatorDetails).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
