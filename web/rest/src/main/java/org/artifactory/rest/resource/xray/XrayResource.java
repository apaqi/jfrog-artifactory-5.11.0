package org.artifactory.rest.resource.xray;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.exception.BadRequestException;
import org.artifactory.rest.common.model.xray.XrayArtifactInfo;
import org.artifactory.rest.common.model.xray.XrayConfigModel;
import org.artifactory.rest.common.model.xray.XrayRepoModel;
import org.artifactory.rest.common.model.xray.XrayScanBuildModel;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.rest.services.ConfigServiceFactory;
import org.artifactory.rest.services.RepoServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.artifactory.rest.common.model.xray.ArtifactXrayModelHelper.getXrayInfo;
import static org.artifactory.rest.resource.xray.XrayResourceHelper.toModel;

/**
 * @author Shay Yaakov
 * @author Chen Keinan
 */
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Path("xray")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class XrayResource extends BaseResource {
    private static final Logger log = LoggerFactory.getLogger(XrayResource.class);
    private static final String anonAccessDisabledMsg = "Anonymous access to build info is disabled";

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private RepoServiceFactory repoServiceFactory;
    @Autowired
    private ConfigServiceFactory configServiceFactory;
    @Autowired
    private AddonsManager addonsManager;

    @POST
    @Path("index")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response index() {
        return runService(repoServiceFactory.indexXray());
    }

    //TODO [by dan]: migrate this to a proper bean
    @POST
    @Path("scanBuild")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.WILDCARD)
    @RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
    public Response scanBuild(XrayScanBuildModel xrayScanBuildModel) {
        if (authorizationService.isAnonUserAndAnonBuildInfoAccessDisabled()) {
            throw new AuthorizationRestException(anonAccessDisabledMsg);
        }
        if (!authorizationService.canDeployToLocalRepository()) {
            throw new AuthorizationRestException();
        }
        CloseableHttpResponse scanBuildResponse = null;
        Response response;
        try {
            scanBuildResponse = addonsManager.addonByType(XrayAddon.class).scanBuild(toModel(xrayScanBuildModel));
        } catch (Exception e) {
            log.debug("Error Executing scan build request: ", e);
        }
        if (scanBuildResponse == null || scanBuildResponse.getEntity() == null
                || scanBuildResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            /*  if no stream found return error */
            log.error("Scan summary report for build name %s number %s is not available, check connectivity to Xray",
                    xrayScanBuildModel.getBuildName(), xrayScanBuildModel.getBuildNumber());
            response = Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity("Scan summary report is not available")
                    .build();
        } else {
            try {
                /* stream response back to client */
                response = XrayResourceHelper.streamResponse(scanBuildResponse);
            } catch (Exception e) {
                IOUtils.closeQuietly(scanBuildResponse);
                log.error("Caught error streaming scan build response to client: ", e);
                response = Response
                        .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                        .entity("Error executing scan build request: " + e.getMessage())
                        .build();
            }
        }
        return response;
    }

    @DELETE
    @Path("clearAllIndexTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearAllIndexTasks() throws Exception {
        return runService(repoServiceFactory.clearAllIndexTasks());
    }

    @GET
    @Path("license")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response license() throws Exception {
        return runService(repoServiceFactory.getXrayLicense());
    }

    @GET
    @Path("{repoKey}/indexStats")
    public Response getIndexStats(@PathParam("repoKey") String repoKey) {
        return runService(repoServiceFactory.getXrayIndexStats());
    }

    @Deprecated //backward compatibility, use getXrayIndexedRepoRepos instead
    @GET
    @Path("repos")
    @RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigureReposIndexing() throws Exception {
        return runService(configServiceFactory.getXrayConfiguredRepos());
    }

    @GET
    @Path("indexRepos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getXrayIndexedRepo() throws Exception {
        return runService(configServiceFactory.getXrayIndexedRepo());
    }

    @PUT
    @Path("indexRepos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response selectXrayIndexedRepos(List<XrayRepoModel> repos) {
        return runService(configServiceFactory.updateXrayIndexRepos(), repos);
    }

    @GET
    @Path("nonIndexRepos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNoneXrayIndexedRepo() throws Exception {
        return runService(configServiceFactory.getNoneXrayIndexedRepo());
    }

    @POST
    @Path("setAlertIgnored")
    public Response resetRepoBlocks(@QueryParam("path") String path) throws Exception {
        RepoPath repoPath = RepoPathFactory.create(path);
        try {
            if (addonsManager.addonByType(XrayAddon.class).setAlertIgnored(repoPath)) {
                return Response.ok().entity("Download will be allowed for path " + path).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("Alert couldn't be ignored. Check logs for more information.").build();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @POST
    @Path("cleanProperties")
    public Response cleanProperties() throws Exception {
        try {
            addonsManager.addonByType(XrayAddon.class).cleanXrayPropertiesFromDB();
            return Response.ok().entity("Xray properties cleanup job has been successfully scheduled").build();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @POST
    @Path("cleanXrayClientCaches")
    public Response cleanCaches() throws Exception {
        try {
            addonsManager.addonByType(XrayAddon.class).cleanXrayClientCaches();
            return Response.ok().entity("Invalidated all Xray client caches").build();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @POST
    @Path("allowBlockedArtifactsDownload")
    public Response allowBlockedArtifactsDownload(@QueryParam("allow") Boolean allow) throws Exception {
        if (allow == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing parameter allow[true|false]").build();
        }
        if (addonsManager.addonByType(XrayAddon.class).updateAllowBlockedArtifactsDownload(allow)) {
            return Response.ok().entity("Xray configuration for allow blocked artifacts download set to: " + allow).build();
        }
        return Response.serverError().entity("Encountered error on config changes. Check logs for more information.").build();
    }

    @POST
    @Path("allowDownloadWhenUnavailable")
    public Response allowDownloadWhenUnavailable(@QueryParam("allow") Boolean allow) throws Exception {
        if (allow == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing parameter allow[true|false]").build();
        }
        if (addonsManager.addonByType(XrayAddon.class).updateAllowDownloadWhenUnavailable(allow)) {
            return Response.ok().entity("Xray configuration for allow download when Xray is unavailable set to: " + allow).build();
        }
        return Response.serverError().entity("Encountered error on config changes. Check logs for more information.").build();
    }

    @GET
    @Path("artifactInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response artifactInfo(@QueryParam("path") String path) {
        RepoPath repoPath = RepoPathFactory.create(path);
        try {
            XrayArtifactInfo xrayInfo = getXrayInfo(addonsManager.addonByType(XrayAddon.class).getArtifactXrayInfo(repoPath));
            return Response.ok().entity(xrayInfo).build();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    //Configuration endpoints

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createXrayConfig(XrayConfigModel xrayConfigModel) throws Exception {
        return runService(configServiceFactory.createXrayConfig(), xrayConfigModel);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateXrayConfig(XrayConfigModel xrayConfigModel) throws Exception {
        return runService(configServiceFactory.updateXrayConfig(), xrayConfigModel);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteXrayConfig() throws Exception {
        return runService(configServiceFactory.deleteXrayConfig());
    }
}
