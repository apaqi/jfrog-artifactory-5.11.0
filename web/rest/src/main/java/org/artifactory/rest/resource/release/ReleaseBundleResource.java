package org.artifactory.rest.resource.release;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.rest.constant.HaRestConstants;
import org.artifactory.api.rest.constant.SystemRestConstants;
import org.artifactory.api.rest.distribution.bundle.models.BundleTransactionResponse;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersionsResponse;
import org.artifactory.api.rest.distribution.bundle.models.BundlesResponse;
import org.artifactory.api.rest.release.ReleaseBundleRequest;
import org.artifactory.api.rest.release.ReleaseBundleResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;

/**
 * @author Shay Bagants
 */
@Path(SystemRestConstants.JFROG_RELEASE)
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReleaseBundleResource {

    private static final String APPLICATION_JOSE = "application/jose";
    private final AddonsManager addonsManager;

    @Autowired
    AuthorizationService authService;

    @Autowired
    public ReleaseBundleResource(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    @Path("bundle")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER, HaRestConstants.ROLE_HA})
    @POST
    public Response assembleBundleFromAql(ReleaseBundleRequest bundleRequest, @QueryParam("includeMetaData") boolean includeMetaData) {
        if (authService.isAnonymous()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        ReleaseBundleResult releaseBundleResult = releaseBundleAddon.executeReleaseBundleRequest(bundleRequest, includeMetaData);
        return Response.ok().entity(releaseBundleResult).build();
    }

    @Path(SystemRestConstants.BUNDLE_TRANSACTION)
    @Consumes("application/jose")
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @POST
    public Response initiateBundleTransaction(String signedJwsBundle) throws IOException, ParseException {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        BundleTransactionResponse response = releaseBundleAddon.createBundleTransaction(signedJwsBundle);
        return Response.status(HttpStatus.SC_CREATED).entity(response).build();
    }

    @Path(SystemRestConstants.BUNDLE_TRANSACTION_CLOSE + "/{transaction_path: .+}")
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @POST
    public Response closeBundleTransaction(@PathParam("transaction_path") String transactionPath) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        releaseBundleAddon.closeBundleTransaction(transactionPath);
        return Response.ok(Collections.EMPTY_MAP).build();
    }

    @Path("bundles")
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @GET
    public Response getBundles() {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        BundlesResponse allBundles = releaseBundleAddon.getAllBundles();
        return Response.ok().entity(allBundles).build();
    }

    @Path("bundles/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @GET
    public Response getBundles(@PathParam("name") String bundleName) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        BundleVersionsResponse bundleVersionsResponse = releaseBundleAddon.getBundleVersions(bundleName);
        return Response.ok().entity(bundleVersionsResponse).build();
    }

    @Path("bundles/{name}/{version}")
    @Produces({MediaType.APPLICATION_JSON, APPLICATION_JOSE})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @GET
    public Response getBundles(@PathParam("name") String bundleName, @PathParam("version") String bundleVersion,
            @QueryParam("format") String format) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        if (StringUtils.isNotBlank(format) && "jws".equalsIgnoreCase(format)) {
            String signedJwsBundle = releaseBundleAddon.getBundleSignedJws(bundleName, bundleVersion);
            return Response.ok().entity(signedJwsBundle).type(APPLICATION_JOSE).build();
        }
        String bundleJson = releaseBundleAddon.getBundleJson(bundleName, bundleVersion);
        return Response.ok().entity(bundleJson).build();
    }

    @Path("bundles/{name}/{version}")
    @Produces({MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    @DELETE
    public Response deleteBundle(@PathParam("name") String bundleName, @PathParam("version") String bundleVersion,
            @QueryParam("include_content") boolean includeContent) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        releaseBundleAddon.deleteReleaseBundle(bundleName, bundleVersion, includeContent);
        return Response.ok().entity(Collections.EMPTY_MAP).build();
    }
}
