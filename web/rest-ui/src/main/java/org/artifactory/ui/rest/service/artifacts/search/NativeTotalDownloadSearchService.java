package org.artifactory.ui.rest.service.artifacts.search;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.NativeDownloadResult;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.artifactory.aql.util.AqlUtils.aggregateRowByPermission;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeSearchHelper.buildVersionItemQuery;

/**
 * @author: ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NativeTotalDownloadSearchService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(NativeTotalDownloadSearchService.class);

    private static final String TYPE = "type";
    private static final String PACKAGE_NAME = "packageName";
    private static final String VERSION_NAME = "versionName";
    private static final String REPO_NAME = "repoKey";


    @Autowired
    private AqlService aqlService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<AqlUISearchModel> searches = new ArrayList<>();
        String type = request.getPathParamByKey(TYPE);
        String packageName = request.getQueryParamByKey(PACKAGE_NAME);
        String versionName = request.getQueryParamByKey(VERSION_NAME);
        String repoName = request.getPathParamByKey(REPO_NAME);

        if (StringUtils.isEmpty(packageName)) {
            response.error("Package name is missing");
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            log.error("Package name is missing");
            return;
        }
        if (StringUtils.isNotEmpty(repoName) && StringUtils.isEmpty(versionName)) {
            response.error("Version name is missing");
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            log.error("version name is missing");
            return;
        }

        List<String> packageValues = new ArrayList<>();
        packageValues.add(packageName);
        searches.add(new AqlUISearchModel("pkg", AqlComparatorEnum.matches, packageValues));
        if (StringUtils.isNotEmpty(versionName)) {
            List<String> versionValues = new ArrayList<>();
            versionValues.add(versionName);
            searches.add(new AqlUISearchModel("version", AqlComparatorEnum.matches, versionValues));
        }
        if (StringUtils.isNotEmpty(repoName)) {
            List<String> repoValues = new ArrayList<>();
            repoValues.add(repoName);
            searches.add(new AqlUISearchModel("repo", AqlComparatorEnum.matches, repoValues));
        }
        NativeDownloadResult model = search(searches, type, packageName);
        response.iModel(model);
    }

    public NativeDownloadResult search(List<AqlUISearchModel> searches, String type, String packageName) {
        AqlBase.PropertyResultFilterClause<AqlApiItem> aqlApiElement = AqlApiItem
                .propertyResultFilter(AqlApiItem.property().key().equal("docker.manifest"));
        AqlApiItem query = buildVersionItemQuery(searches, type, "", "", true, aqlApiElement);
        if (log.isDebugEnabled()) {
            log.debug("strategies resolved to query: " + query.toNative(0));
        }
        int totalDownloads = executeSearch(query, packageName);
        return new NativeDownloadResult(totalDownloads);
    }

    private int executeSearch(/*AqlDomainEnum domain,*/ AqlBase query, String packageName) {
        long timer = System.currentTimeMillis();
        List<AqlItem> queryResults = aqlService.executeQueryEager(query).getResults();
        queryResults = aggregateRowByPermission(queryResults);
        int totalDownloads = aggregateDownloadsResultsPackage(queryResults);
        log.debug("Search total downloads for package named {} in {} milliseconds total {}", packageName,
                System.currentTimeMillis() - timer, totalDownloads);
        return totalDownloads;
    }

    private int aggregateDownloadsResultsPackage(List<AqlItem> queryResults) {
        return queryResults.stream().map(aql -> ((AqlBaseFullRowImpl)aql).getDownloads()).reduce(0, (a, b) -> a + b);
        //return queryResults.stream().mapToInt(AqlBaseFullRowImpl::getDownloads).sum();
    }
}
