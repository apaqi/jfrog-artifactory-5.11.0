/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.db.aql.itest.service;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.result.AqlEagerResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gidi Shabat
 */
public class AqlComparatorTest extends AqlAbstractServiceTest {

    private static final String INVALID_SYNTAX = "Illegal syntax the 'null' values are allowed to use only with equals and not equals operators.\n";

    @Test
    public void greaterWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$gt\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void greaterEqualsWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$gte\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void greaterLessWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$lt\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void lessEqualsWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$lte\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void matchWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$match\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void notMatchWithNull() {
        try {
            AqlEagerResult queryResult = aqlService.executeQueryEager(
                    "items.find({\"repo\" :\"repo1\",\"stat.downloads\":{\"$nmatch\":null}})");
            assertSize(queryResult, 3);
        } catch (AqlException e) {
            Assert.assertEquals(e.getMessage(), INVALID_SYNTAX);
        }
    }

    @Test
    public void equalWithNull() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"modified\":{\"$eq\": null }})");
        assertSize(queryResult, 0);
    }

    @Test
    public void notEqualWithNull() {
        AqlEagerResult queryResult = aqlService.executeQueryEager(
                "items.find({\"repo\" :\"repo1\",\"modified\":{\"$ne\": null }})");
        assertSize(queryResult, 4);
    }
}
