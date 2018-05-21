package org.artifactory.ui.rest.model.artifacts.search.packagesearch.result;

import com.google.common.collect.Sets;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.model.AqlNativePkgVersion;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.PackageNativeSearchService;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

/**
 * @author: ortalh
 */
@Data
@Component
public class PackageNativeResult {

    private static final Logger log = LoggerFactory.getLogger(PackageNativeResult.class);

    private String name;
    private Set<String> repositories;
    private int numOfRepos;

    public PackageNativeResult(String pkgName, AqlBaseFullRowImpl row) {
        this.name = pkgName;
        String repo = row.getRepo();
        this.repositories = Sets.newTreeSet();
        this.repositories.add(repo);
    }

    public PackageNativeResult() {
    }

    /**
     * Used by the {@link PackageNativeSearchService}'s reduction mechanism to aggregate result rows into one result
     * NOTE: assumes this model is already constructed using a row representing the same path as all rows being
     * aggregated.
     */
    public PackageNativeResult aggregateRow(AqlBaseFullRowImpl row) {
        //Row contains a property
        String repoKey = row.getRepo();

        if (CollectionUtils.isNullOrEmpty(repositories)) {
            repositories = Sets.newTreeSet();
            repositories.add(repoKey);
        }

        //Only add repoKey that correlate to the same package path
        log.debug("Found matching package {} and Re, aggregating into result", name);
        repositories.add(repoKey);
        numOfRepos = repositories.size();
        return this;
    }

    public static PackageNativeResult merge(PackageNativeResult res1, PackageNativeResult res2) {
        if (CollectionUtils.isNullOrEmpty(res1.repositories) && CollectionUtils.notNullOrEmpty(res2.repositories)) {
            res1.setRepositories(res2.repositories);
        }
        if (StringUtils.isBlank(res1.getName()) && StringUtils.isNotBlank(res2.getName())) {
            res1.setName(res2.getName());
        }
        if (res1.name.equals(res2.name)) {
            if (!res1.repositories.isEmpty()) {
                res1.repositories.addAll(res2.repositories);
            }
        }
        return res1;
    }
}
