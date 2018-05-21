package org.artifactory.ui.rest.resource.admin.security.keys;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.security.keys.KidIModel;
import org.artifactory.ui.rest.model.admin.security.keys.TrustedKeyIModel;
import org.artifactory.ui.rest.service.admin.security.keys.TrustedKeysServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


/**
 * @author Inbar Tal
 */
@Path("security/trustedKeys")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TrustedKeysUIResource extends BaseResource {

    @Autowired
    TrustedKeysServiceFactory trustedKeysService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getAllKeys() {
        return runService(trustedKeysService.getAllTrustedKeys());
    }

    @GET
    @Path("{kid: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getKeyInfoById(@PathParam("kid") String kid) {
        return runService(trustedKeysService.getTrustedKeyById(), kid);
    }

    @GET
    @Path("validate/{key_alias: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateKeyAlias(@PathParam("key_alias") String keyAlias) {
        return runService(trustedKeysService.validateTrustedKey(), keyAlias);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response createKey(TrustedKeyIModel trustedKey) throws Exception {
        return runService(trustedKeysService.createTrustedKey(), trustedKey);
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response deleteKeysById(List<KidIModel> trustedKey) throws Exception {
        return runService(trustedKeysService.deleteTrustedKeys(), trustedKey);
    }

}
