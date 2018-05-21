package org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;

/**
 * @author: ortalh
 */

public class AqlUIDockerV2TagPathSearchStrategy extends AqlUIFieldSearchStrategy {

    public AqlUIDockerV2TagPathSearchStrategy(AqlPhysicalFieldEnum field, AqlDomainEnum[] subdomainPath) {
        super(field, subdomainPath);
    }

    @Override
    public AqlBase.OrClause toQuery() {
        AqlBase.OrClause query = AqlApiItem.or();
        AqlBase.AndClause and = AqlApiItem.and();
        and.append(new AqlBase.CriteriaClause(AqlPhysicalFieldEnum.itemName, Lists.newArrayList(AqlDomainEnum.items),
                AqlComparatorEnum.equals, "manifest.json"));
        values.forEach(value -> and.append(
                new AqlBase.CriteriaClause(field, Lists.newArrayList(AqlDomainEnum.items), comparator, value)));

        query.append(and);
        return query;
    }
}
