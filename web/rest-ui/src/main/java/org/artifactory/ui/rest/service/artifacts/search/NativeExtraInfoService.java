package org.artifactory.ui.rest.service.artifacts.search;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.ExtraInfoNativeResult;
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
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeSearchHelper.buildVersionItemQueryByPath;

/**
 * @author: ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NativeExtraInfoService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(NativeExtraInfoService.class);

    private static final String TYPE = "type";
    private static final String PACKAGE_NAME = "packageName";

    @Autowired
    private AqlService aqlService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<AqlUISearchModel> searches = new ArrayList<>();
        String type = request.getPathParamByKey(TYPE);
        String packageName = request.getQueryParamByKey(PACKAGE_NAME);

        if (StringUtils.isEmpty(packageName)) {
            response.error("Package name is missing");
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            log.error("Package name is missing");
            return;
        }
        boolean searchByPath = ConstantValues.packageNativeUiSearchByPath.getBoolean();
        if (searchByPath) {
            List<String> pathValues = new ArrayList<>();
            pathValues.add(packageName + "/*");
            searches.add(new AqlUISearchModel("path", AqlComparatorEnum.matches, pathValues));
        } else {
            List<String> packageValues = new ArrayList<>();
            packageValues.add(packageName);
            searches.add(new AqlUISearchModel("pkg", AqlComparatorEnum.matches, packageValues));
        }

        ExtraInfoNativeResult model = search(searches, type, packageName, searchByPath);
        response.iModel(model);
    }

    public ExtraInfoNativeResult search(List<AqlUISearchModel> searches, String type, String packageName, boolean searchByPath) {
        boolean includeDownloadInResult = ConstantValues.packageNativeUiIncludeTotalDownload.getBoolean();
        AqlApiItem query;
        if (!searchByPath) {
            AqlBase.PropertyResultFilterClause<AqlApiItem> aqlApiElement = AqlApiItem
                    .propertyResultFilter(AqlApiItem.property().key().equal("docker.manifest"));
            query = buildVersionItemQuery(searches, type, "", "", includeDownloadInResult, aqlApiElement);
        } else {
            query = buildVersionItemQueryByPath(searches, includeDownloadInResult);
        }
        if (log.isDebugEnabled()) {
            log.debug("strategies resolved to query: " + query.toNative(0));
        }
        return executeSearch(query, packageName, includeDownloadInResult);
    }

    private ExtraInfoNativeResult executeSearch(/*AqlDomainEnum domain,*/ AqlApiItem query, String packageName,
            boolean includeTotalDownloads) {
        long timer = System.currentTimeMillis();
        List<AqlItem> results = aqlService.executeQueryEager(query).getResults();
        results = aggregateRowByPermission(results);
        ExtraInfoNativeResult extraInfoNativeResult = aggregateDownloadsResultsPackage(results, includeTotalDownloads);
        log.debug("Search total downloads for package named {} in {} milliseconds total {}", packageName,
                System.currentTimeMillis() - timer, extraInfoNativeResult);
        return extraInfoNativeResult;
    }

    private ExtraInfoNativeResult aggregateDownloadsResultsPackage(List<AqlItem> queryResults,
            boolean includeDownloadInResult) {
        ExtraInfoNativeResult extraInfoNativeResult = new ExtraInfoNativeResult();
        if (includeDownloadInResult) {
            extraInfoNativeResult.setTotalDownloads(0);
        }
        queryResults
                .forEach(queryResult -> extraInfoNativeResult.setExtraInfoByRow(queryResult, includeDownloadInResult));
        return extraInfoNativeResult;
    }
}
