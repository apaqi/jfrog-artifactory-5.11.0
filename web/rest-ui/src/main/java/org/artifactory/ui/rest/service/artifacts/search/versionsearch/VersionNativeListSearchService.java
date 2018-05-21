package org.artifactory.ui.rest.service.artifacts.search.versionsearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.SearchNativeVersionListResult;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionNativeResult;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker.DockerV2ManifestBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeSearchHelper.buildVersionItemQuery;

/**
 * @author: ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeListSearchService extends DockerV2ManifestBaseService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(VersionNativeListSearchService.class);
    private static final String TYPE = "type";
    private static final String PACKAGE_NAME = "packageName";
    private static final String SORT_BY = "sort_by";
    private static final String ORDER = "order";

    @Autowired
    private AqlService aqlService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<AqlUISearchModel> searches = (List<AqlUISearchModel>) request.getModels();
        String type = request.getPathParamByKey(TYPE);
        String packageName = request.getQueryParamByKey(PACKAGE_NAME);
        String sort = request.getQueryParamByKey(SORT_BY);
        String order = request.getQueryParamByKey(ORDER);

        if (StringUtils.isEmpty(packageName)) {
            response.error("Package name is missing");
            response.responseCode(400);
            log.error("Package name is missing");
            return;
        }

        List<String> values = Lists.newArrayList(packageName);

        searches.add(new AqlUISearchModel("pkg", AqlComparatorEnum.matches, values));
        SearchNativeVersionListResult model = search(searches, packageName, type, sort, order, response);
        response.iModel(model);
    }

    public SearchNativeVersionListResult search(List<AqlUISearchModel> searches, String packageName, String type,
            String sort, String order, RestResponse response) {
        AqlBase.PropertyResultFilterClause<AqlApiItem> firstFilter = AqlApiItem
                .propertyResultFilter(AqlApiItem.property().key().equal("docker.manifest"));
        AqlBase.PropertyResultFilterClause<AqlApiItem> secondFilter = AqlApiItem
                .propertyResultFilter(AqlBase.or(AqlApiItem.property().key().equal("docker.manifest.digest"), AqlApiItem.property().key().equal("sha256")));

        AqlApiItem aqlApiItem = buildVersionItemQuery(searches, type, sort, order, true, firstFilter);
        AqlApiItem aqlApiItemWithDigest = buildVersionItemQuery(searches, type, sort, order, true, secondFilter);
        if (log.isDebugEnabled()) {
            log.debug("strategies resolved to query: " + aqlApiItem.toNative(0));
            log.debug("strategies resolved to query: " + aqlApiItemWithDigest.toNative(0));
        }
        SearchNativeVersionListResult searchVersionNativeResult = executeSearch(packageName, aqlApiItem, aqlApiItemWithDigest);
        return searchVersionNativeResult;
    }

    private SearchNativeVersionListResult  executeSearch(String packageName, AqlApiItem query, AqlApiItem queryWithDigest) {
        long timer = System.currentTimeMillis();
        List<AqlItem> queryResults = aqlService.executeQueryEager(query).getResults();
        Map<Long, String> resultsWithDigest = Maps.newHashMap();
                aqlService.executeQueryEager(queryWithDigest).getResults().stream()
                .filter(item -> isNotBlank(((AqlBaseFullRowImpl) item).getValue()))
                .forEach(item -> resultsWithDigest.putIfAbsent(item.getNodeId(), ((AqlBaseFullRowImpl) item).getValue()));

        List<AqlItem> resultsByPath = AqlUtils.aggregateRowByPermission(queryResults);

        SearchNativeVersionListResult results = reduceAggregatedResultRows(packageName,resultsByPath, resultsWithDigest);
        log.trace("Search found {} results in {} milliseconds", results.getResultsCount(), System.currentTimeMillis() - timer);
        return results;
    }

    //TODO [by shayb]: This is a quick fix for the 5.10 release, we must move the docker-logic from here, this should be generic code
    //TODO [by dan]: refactor this hellish code before the lords of the underworld reach out and grab us to suffer in brimstone for all eternity
    private SearchNativeVersionListResult  reduceAggregatedResultRows(String packageName, List<AqlItem> resultsByPath, Map<Long, String> resultsWithDigest ) {
        SearchNativeVersionListResult result = new SearchNativeVersionListResult();
        result.setLastModified(new Date(0));
        List<VersionNativeResult> versions = Lists.newArrayList();
        resultsByPath.forEach(item -> {
            String name = ((AqlBaseFullRowImpl) item).getValue();
            String repoKey = item.getRepo();
            Date lastModified = item.getModified();
            String digest = resultsWithDigest.get(item.getNodeId());
            String manifestDigest = getPackageIdFromDigestProperty(digest);
            VersionNativeResult nativeResult = new VersionNativeResult(name, manifestDigest, repoKey, lastModified);
            versions.add(nativeResult);
            if (result.getLastModified().before(nativeResult.getLastModified())) {
                result.setLastModified(nativeResult.getLastModified());
            }
        });
        result.setPackageName(packageName);
        result.setResultsCount(versions.size());
        result.setVersions(versions);
        return result;
    }
}
