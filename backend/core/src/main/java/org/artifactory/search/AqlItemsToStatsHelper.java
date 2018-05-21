package org.artifactory.search;

import org.artifactory.api.search.stats.StatsSearchResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.service.InternalRepositoryService;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Saffi Hartal
 * Helper for getting artifact stats using VisibleAqlItemsSearchHelper as stream
 */
public class AqlItemsToStatsHelper {

    private final VisibleAqlItemsSearchHelper visibleAqlItemsHelper;

    public AqlItemsToStatsHelper(@Nonnull VisibleAqlItemsSearchHelper visibleAqlItemsHelper) {
        this.visibleAqlItemsHelper = visibleAqlItemsHelper;
    }

    /**
     * stats conversion
     *
     * @param stream
     * @return
     */
    public Stream<StatsSearchResult> toStatsSearchResultStream(Stream<AqlItem> stream) {
        InternalRepositoryService repoService = visibleAqlItemsHelper.repoService;

        Function<ItemInfo, StatsSearchResult> itemInfoToStatsSearchResult =
                item -> new StatsSearchResult(item, repoService.getStatsInfo(item.getRepoPath()));

        return visibleAqlItemsHelper.visibleItemInfo(stream).map(itemInfoToStatsSearchResult);
    }
}
