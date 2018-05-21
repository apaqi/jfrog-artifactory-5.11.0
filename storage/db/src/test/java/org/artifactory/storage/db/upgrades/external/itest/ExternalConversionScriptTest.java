package org.artifactory.storage.db.upgrades.external.itest;

import org.artifactory.storage.db.itest.DbTestUtils;
import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.testng.annotations.Test;

import java.sql.Connection;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 * This test makes the {@link org.artifactory.storage.db.version.converter.DbSqlConverterUtil} invoke an external
 * conversion script.
 */
@Test
public class ExternalConversionScriptTest extends UpgradeBaseTest {

    public void testExternalConversionScriptRan() throws Exception {
        // This creates an alternative derby_v570.sql file that creates a dummy column
        prepareTestInstanceForExternalConversion(false);
        try (Connection connection = jdbcHelper.getDataSource().getConnection()) {
            // Expect our dummy column to exist
            assertTrue(DbTestUtils.isColumnExist(connection, "users", "foo"));
        }
    }

    public void testExternalEmptyConversionScriptRan() throws Exception {
        // This creates an alternative derby_v570.sql file that creates a dummy column
        prepareTestInstanceForExternalConversion(true);
        try (Connection connection = jdbcHelper.getDataSource().getConnection()) {
            // Expect our dummy column to not exist
            assertFalse(DbTestUtils.isColumnExist(connection, "users", "foo"));
        }
    }
}
