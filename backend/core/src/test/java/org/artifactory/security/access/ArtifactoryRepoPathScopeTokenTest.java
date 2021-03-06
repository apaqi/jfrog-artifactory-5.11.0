package org.artifactory.security.access;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author nadavy
 */
public class ArtifactoryRepoPathScopeTokenTest {

    @Test(dataProvider = "provideValidScopeTokenAndRepoPaths")
    public void testParse(String scopeToken, String repoPath, String permissions) throws Exception {
        ArtifactoryRepoPathScopeToken parsedToken = ArtifactoryRepoPathScopeToken.parse(scopeToken);
        assertEquals(parsedToken.getRepoPathChecksum(), repoPath);
        assertEquals(parsedToken.getPermissions(), permissions);
    }

    @DataProvider
    public static Object[][] provideValidScopeTokenAndRepoPaths() {
        return new Object[][]{
                {"path:repo/path/artifact.json:read", "861e50591a5b6c1a267ff8807bbb3b0d7255a709", "read"},
                {"path:repo/artifact.json:read", "d125d9f52ed71e863866c59ea4c3272a231c036d", "read"},
                {"path.checksum:d125d9f52ed71e863866c59ea4c3272a231c036d:read", "d125d9f52ed71e863866c59ea4c3272a231c036d", "read"},
                {"path:repo/artifact.json:read", "d125d9f52ed71e863866c59ea4c3272a231c036d", "read"}
        };
    }

    @Test(dataProvider = "provideScopeTokens")
    public void testAccepts(String scopeToken, boolean expected) throws Exception {
        assertEquals(ArtifactoryRepoPathScopeToken.accepts(scopeToken), expected);
    }

    @DataProvider
    public static Object[][] provideScopeTokens() {
        return new Object[][]{
                {"path:repo/path/artifact.json:read", true},
                {"path:repo:read", true},
                {"path:/repo/:delete", false},
                {"path:repo/path/artifact.json:", false},
                {"path:repo/path/artifact.json:read", true},
                {"path:repo/path/artifact.json:rdw", false},
                {"path:repo/path/artifact.json,repo:r", false},
                {"path.checksum:817053145b10a4e65f62b7523ce1d086:read", true},
                {"path.checksum:817053145b10a4e65f62b7523ce1d086:delete", false},
                {"path.checksum:817053145b10a4e65f62b7523ce1d086:d", false},
                {"path:repo/path/artifact.json,repo,repo/path2/artifact2.json:r", false}
        };
    }
}