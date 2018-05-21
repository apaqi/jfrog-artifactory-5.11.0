package org.artifactory.ui.rest.resource.artifacts.versions;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.service.artifacts.browse.BrowseServiceFactory;
import org.artifactory.ui.rest.service.artifacts.search.SearchServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author: ortalh
 */
@Path("v1/versions")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeResource extends BaseResource {

    @Autowired
    private BrowseServiceFactory browseServiceFactory;
    @Autowired
    private SearchServiceFactory searchFactory;

    @GET
    @Path("docker/{repoKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nativeVersionManifestSearch() throws Exception {
        return runService(browseServiceFactory.dockerV2ViewNativeService());
    }

    @GET
    @Path("/total_downloads/{type}/{repoKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nativeVersionDownloadSearch() throws Exception {
        return runService(searchFactory.nativeTotalDownloadSearchService());
    }
}
