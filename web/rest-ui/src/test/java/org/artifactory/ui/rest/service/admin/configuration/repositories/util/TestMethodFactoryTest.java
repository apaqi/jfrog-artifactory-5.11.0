package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.http.client.methods.HttpRequestBase;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertEquals;


public class TestMethodFactoryTest {

    private final String npmRepoUrl = "https://registry.npmjs.org/";

    @BeforeMethod
    public void setup() throws Exception {
        AddonsManager addonsManager = createMock(AddonsManager.class);
        HaCommonAddon haCommonAddon = createMock(HaCommonAddon.class);
        expect(addonsManager.addonByType(HaCommonAddon.class)).andReturn(haCommonAddon).anyTimes();
        expect(haCommonAddon.getHostId()).andReturn("4").anyTimes();

        ArtifactoryContext context = createMock(ArtifactoryContext.class);
        expect(context.beanForType(AddonsManager.class)).andReturn(addonsManager).anyTimes();

        replay(addonsManager, haCommonAddon, context);
        ArtifactoryContextThreadBinder.bind(context);
    }

    @Test(dataProvider = "getFakeCredentialsAndExpectedValues")
    public void testCreateTestMethodForNpmWithOrWithoutCredentials(String fakeUsername, String fakePassword,
            String expectedMethod, String expectedUrl) throws Exception {
        //arrange
        RemoteNetworkRepositoryConfigModel mockedNetworkModel = createMock(RemoteNetworkRepositoryConfigModel.class);
        expect(mockedNetworkModel.getUsername()).andReturn(fakeUsername);
        expect(mockedNetworkModel.getPassword()).andReturn(fakePassword);
        replay(mockedNetworkModel);
        //act
        final HttpRequestBase request = TestMethodFactory
                .createTestMethod(npmRepoUrl, RepoType.Npm, null, mockedNetworkModel);
        //assert
        assertEquals(request.getMethod(), expectedMethod);
        assertEquals(request.getURI().toString(), expectedUrl);
    }

    @Test
    public void testCreateTestMethodForNpmWithNetworkModelNull() throws Exception {
        //arrange

        //act
        final HttpRequestBase request = TestMethodFactory
                .createTestMethod(npmRepoUrl, RepoType.Npm, null, null);
        //assert
        assertEquals(request.getMethod(), "HEAD");
        assertEquals(request.getURI().toString(), npmRepoUrl);
    }

    @DataProvider
    public Object[][] getFakeCredentialsAndExpectedValues() {
        return new Object[][]{
                {"fakeUsername", "fakePassword", "PUT", npmRepoUrl + "-/user/org.couchdb.user:fakeUsername"},
                {"fakeUsername", "", "HEAD", npmRepoUrl},
                {"fakeUsername", null, "HEAD", npmRepoUrl},
                {"", "fakePassword", "HEAD", npmRepoUrl},
                {null, "fakePassword", "HEAD", npmRepoUrl},
                {null, null, "HEAD", npmRepoUrl},
                {"", "", "HEAD", npmRepoUrl}
        };
    }
}