package org.artifactory.storage.db.aql.itest.service.actions;

import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.storage.db.aql.itest.service.AqlAbstractServiceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.artifactory.aql.api.internal.AqlBase.and;

@Test(enabled = false)
public class AqlResultStreamTest extends AqlAbstractServiceTest {
    private static final Logger log = LoggerFactory.getLogger(AqlResultStreamTest.class);

    public void findBuildWithItemsAndItemProperties() {
        final boolean[] done = {false};
        AqlBase.AndClause<AqlApiItem> query = and();
        query.append(AqlApiItem.size().greater(5));
        AqlApiItem aqlQuery = AqlApiItem.create().filter(query);

        AqlLazyResult<AqlItem> aqlLazyResult = aqlService.executeQueryLazy(aqlQuery);
        Stream<AqlItem> stream = aqlLazyResult.asStream((e) -> {
            if (e == null) {
                done[0] = true;
                log.info("done ");
            } else throw new RuntimeException(e);
        });
        Consumer<AqlItem> aqlItemConsumer = (item) -> log.info("item: {}", item.toString());
        Assert.assertEquals(done[0], false);
        stream.forEach(aqlItemConsumer);
        Assert.assertEquals(done[0], true);
        stream.close();
        Assert.assertEquals(done[0], true);
    }
}
