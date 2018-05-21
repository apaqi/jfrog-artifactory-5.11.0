package org.artifactory.ui.rest.resource.artifacts.packages;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.SearchServiceFactory;
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
 * @author: ortalh
 */
@Path("v1/packages")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PackageNativeResource extends BaseResource {

    @Autowired
    private SearchServiceFactory searchFactory;

    @POST
    @Path("/{type}/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nativePackageSearch(List<AqlUISearchModel> search) throws Exception {
        return runService(searchFactory.packageNativeSearchService(), search);
    }

    @POST
    @Path("{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nativePackageSearchWithSpecifiedRepos(List<AqlUISearchModel> search) throws Exception {
        return runService(searchFactory.versionNativeListSearchService(), search);
    }

    @GET
    @Path("/total_downloads/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nativePackageDownloadSearch() throws Exception {
        return runService(searchFactory.nativeTotalDownloadSearchService());
    }

    @GET
    @Path("/extra_info/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nativePackageExtraInfoSearch() throws Exception {
        return runService(searchFactory.nativeExtraInfoService());
    }
}
