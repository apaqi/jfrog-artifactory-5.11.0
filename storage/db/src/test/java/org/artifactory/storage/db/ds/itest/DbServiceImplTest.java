package org.artifactory.storage.db.ds.itest;

import org.artifactory.storage.db.DbServiceImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.util.JdbcHelper;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

@Test
@ContextConfiguration(locations = {"classpath:spring/db-test-context.xml"})
public class DbServiceImplTest extends DbBaseTest {


    @Autowired
    private DbServiceImpl dbService;

    @Mock
    private JdbcHelper jdbcHelper;

    @Mock
    private DataSource dataSource;

    @BeforeClass
    private void beforeClass() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRetryConnection() throws Exception {
        Mockito.when(dataSource.getConnection()).thenThrow(new SQLException("Testing retry connection"));
        Mockito.when(jdbcHelper.getDataSource()).thenReturn(dataSource);
        ReflectionTestUtils.setField(dbService, "jdbcHelper", jdbcHelper);

        Assert.expectThrows(SQLException.class, () -> dbService.initDb());
        Mockito.verify(dataSource, Mockito.times(3)).getConnection();
    }
}
