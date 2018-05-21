/*
 *
 * Copyright 2016 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.search;

import org.artifactory.api.search.SearchControls;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.AqlConverts;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.storage.spring.StorageContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.artifactory.aql.api.internal.AqlBase.or;

/**
 * @author Saffi Hartal
 * Search for AqlItem stream filtered to visible real items (not checksums)
 */
public class VisibleAqlItemsSearchHelper {

    private static final Logger log = LoggerFactory.getLogger(VisibleAqlItemsSearchHelper.class);
    public final InternalRepositoryService repoService;
    public final Predicate<AqlItem> isVisibleNotChecksumLink = this::isVisibleAndNotChecksumLink;
    private final AuthorizationService authService;


    public VisibleAqlItemsSearchHelper(@Nonnull AuthorizationService authService, @Nonnull InternalRepositoryService repoService) {
        this.repoService = repoService;
        this.authService = authService;
    }


    /**
     * stats conversion
     *
     * @param stream
     * @return
     */
    public Stream<ItemInfo> visibleItemInfo(Stream<AqlItem> stream) {
        return stream.filter(isVisibleNotChecksumLink).map(AqlConverts.toItemInfo);
    }

    /**
     * Indicates whether the given result repo path is valid or not.<br> A repo path will stated as valid if the given
     * origin local repo is not null, the path is readable (permission and repo-configuration-wise) and if it is not a
     * checksum file.
     *
     * @param item AqlItem has  Repopath to validate
     * @return True if the repo path is valid
     */
    private boolean isVisibleAndNotChecksumLink(AqlItem item) {
        RepoPath repoPath = AqlConverts.toRepoPath.apply(item);
        LocalRepo repo = repoService.localOrCachedRepositoryByKey(repoPath.getRepoKey());
        return repo != null && isVisibleNotChecksumLink(repoPath);
    }

    /**
     * Search Acceptable item info ( visible repo path + not checksum link type)
     *
     * @param query    AqlBase.AndClause<AqlApiItem>
     * @param onFinish Consumer<Exception>
     */
    public Stream<AqlItem> searchVisibleNonChecksumLinkAsStream(AqlBase.AndClause<AqlApiItem> query, Consumer<Exception> onFinish) {
        Integer limit = isQueryLimited() ? getQueryLimit() : null;
        return searchAsStream(query, limit, onFinish).filter(isVisibleNotChecksumLink);
    }

    /**
     * general aql item search streamed
     *
     * @param query    AqlBase.AndClause<AqlApiItem>
     * @param onFinish Consumer<Exception>
     * @return Stream<AqlItem>
     */
    private Stream<AqlItem> searchAsStream(AqlBase.AndClause<AqlApiItem> query, Consumer<Exception> onFinish) {
        return lazyQuery(query, null).asStream(onFinish);
    }

    private Stream<AqlItem> searchAsStream(AqlBase.AndClause<AqlApiItem> query, Integer limit, Consumer<Exception> onFinish) {
        return lazyQuery(query, limit).asStream(onFinish);
    }


    /**
     * @return a clause which will filter results based on {@link SearchControls#getSelectedRepoForSearch()}
     * The artifact should exist in at least one of the repositories therefore the relation between the repos is OR
     */
    public AqlBase.OrClause<AqlApiItem> getSelectedReposForSearchClause(SearchControls controls) {
        List<String> selectedRepoForSearch = controls.getSelectedRepoForSearch();
        AqlBase.OrClause<AqlApiItem> reposToSearchClause = or();
        if (selectedRepoForSearch != null) {
            for (String repoKey : selectedRepoForSearch) {
                reposToSearchClause.append(
                        AqlApiItem.repo().equal(repoKey)
                );
            }
        }
        return reposToSearchClause;
    }


    private AqlLazyResult<AqlItem> lazyQuery(AqlBase.AndClause<AqlApiItem> query, Integer limit) {
        AqlApiItem aqlQuery = AqlApiItem.create().filter(query);
        AqlService aqlService = StorageContextHelper.get().beanForType(AqlService.class);
        if (limit != null) {
            aqlQuery.limit(limit);
        }
        return aqlService.executeQueryLazy(aqlQuery);
    }


    /**
     * Indicates whether the given result repo path is valid or not.<br> A repo path will stated as valid if the given
     * origin local repo is not null, the path is readable (permission and repo-configuration-wise) and if it is not a
     * checksum file.
     *
     * @param repoPath Repo path to validate
     * @return True if the repo path is valid
     */
    private boolean isVisibleNotChecksumLink(RepoPath repoPath) {
        return repoService.isRepoPathVisible(repoPath) && (!NamingUtils.isChecksum(repoPath.getPath()));
    }

    private int getQueryLimit() {
        boolean isLimited = isQueryLimited();
        return isLimited ? ConstantValues.searchUserQueryLimit.getInt() : Integer.MAX_VALUE;

    }

    private boolean isQueryLimited() {
        return ConstantValues.searchLimitAnonymousUserOnly.getBoolean() ?
                authService.isAnonymous() : !authService.isAdmin();
    }
}
