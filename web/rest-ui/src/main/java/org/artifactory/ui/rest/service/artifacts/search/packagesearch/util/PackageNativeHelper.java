package org.artifactory.ui.rest.service.artifacts.search.packagesearch.util;

import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.common.ConstantValues;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.FieldSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy.AqlUISearchStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.domain.sensitive.AqlApiItem.statistic;

/**
 * @author: ortalh
 */
public class PackageNativeHelper {
    private static final Logger log = LoggerFactory.getLogger(PackageNativeHelper.class);

    public static AqlBase buildItemQuery(List<AqlUISearchModel> searches, String type, boolean includeDownloadInResult) {
        List<AqlUISearchStrategy> strategies = buildStrategiesFromSearchModel(searches, type);
        log.debug("input searches resolved to the following strategies: " + Arrays.toString(strategies.toArray()));

        AqlApiItem.AndClause query = AqlApiItem.and();
        strategies.stream()
                .map(AqlUISearchStrategy::toQuery)
                .filter(orClause -> !orClause.isEmpty())
                .forEach(query::append);



        int numOfResults = 10000;
        AqlApiItem aql = AqlApiItem.create().filter(query);
        if(includeDownloadInResult){
            aql.include(statistic().downloads());
        }
        //Result set size limit is calculated by (max ui results) * no of property keys being searched
        // == UI result limit (i.e. 500)repo paths + all of the required properties that will be shown in UI.
        log.debug("Total number for current native package type being searched is {}," +
                numOfResults);
        return aql.limit(numOfResults);
    }


    private static List<AqlUISearchStrategy> buildStrategiesFromSearchModel(List<AqlUISearchModel> searches,
            String type) {
        return searches.stream()
                .map(search -> getStrategyByTypeAndElementTypeOrByFieldId(search, type)
                        .comparator(search.getComparator())
                        .values(search.getValues()))
                .collect(Collectors.toList());
    }

    /**
     * Tries each of the criteria enums for the strategy of the given model
     */
    private static AqlUISearchStrategy getStrategyByTypeAndElementTypeOrByFieldId(AqlUISearchModel search,
            String type) {
        switch (search.getId()) {
            case "pkg":
                if (type.equals("docker")) {
                    return PackageSearchCriteria.getStrategyByTypeAndElementType(
                            PackageSearchCriteria.PackageSearchType.valueOf("dockerV2"), "Image");
                }
                return PackageSearchCriteria.getStrategyByTypeAndElementType(
                        PackageSearchCriteria.PackageSearchType.valueOf(search.getId()), "Name");
            case "version":
                if (type.equals("docker")) {
                    return PackageSearchCriteria.getStrategyByTypeAndElementType(
                            PackageSearchCriteria.PackageSearchType.valueOf("dockerV2"), "Tag");
                }
                return PackageSearchCriteria.getStrategyByTypeAndElementType(
                        PackageSearchCriteria.PackageSearchType.valueOf(search.getId()), "Version");
            case "repo":
                return FieldSearchCriteria.getStrategyByFieldId(search.getId());
            default:
                throw new IllegalArgumentException("Not a valid id value :"+ search.getId());
        }
    }
}