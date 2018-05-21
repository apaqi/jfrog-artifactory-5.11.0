package org.artifactory.security.access.emigrate.conveter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.security.access.AccessService;
import org.artifactory.storage.db.security.service.access.UserPropertiesSearchHelper;
import org.artifactory.test.ArtifactoryHomeStub;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.user.UsersClient;
import org.jfrog.access.rest.user.UpdateUserRequest;
import org.jfrog.access.rest.user.User;
import org.jfrog.access.rest.user.Users;
import org.jfrog.security.crypto.CipherAlg;
import org.jfrog.security.file.SecurityFolderHelper;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Shemesh
 */
public class V5100DecryptAllUsersCustomDataTest {
    @Mock
    private AccessService accessService;

    private V5100DecryptAllUsersCustomData decryptAllUsersCustomData;

    @BeforeMethod
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);
        ArtifactoryHome artifactoryHome = new ArtifactoryHomeStub();
        artifactoryHome.getArtifactoryKey().delete();
        SecurityFolderHelper.createKeyFile(artifactoryHome.getArtifactoryKey(), CipherAlg.AES128);
        ArtifactoryHome.bind(artifactoryHome);
        decryptAllUsersCustomData = new V5100DecryptAllUsersCustomData(accessService);
    }

    @AfterMethod
    public void afterMethod() {
        ArtifactoryHome.get().getArtifactoryKey().delete();
    }

    @Test
    public void testDecryptUserProperties() {
        assertTrue(CryptoHelper.hasArtifactoryKey(ArtifactoryHome.get()));
        AccessClient accessClient = Mockito.mock(AccessClient.class);
        UsersClient usersMock = Mockito.mock(UsersClient.class);
        Users usersResponseMock = Mockito.mock(Users.class);
        when(accessService.getAccessClient()).thenReturn(accessClient);
        when(accessClient.users()).thenReturn(usersMock);
        when(usersMock.findUsers()).thenReturn(usersResponseMock);

        User userMock = Mockito.mock(User.class);
        when(usersResponseMock.getUsers()).thenReturn(ImmutableList.of(userMock));
        when(userMock.getUsername()).thenReturn("noam");
        when(userMock.getCustomData()).thenReturn(ImmutableMap.of("key", "regularValue", "apiKey",
                CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), "encrypt")));

        decryptAllUsersCustomData.convert();

        ArgumentCaptor<UpdateUserRequest> captor = ArgumentCaptor.forClass(UpdateUserRequest.class);
        verify(usersMock).updateUser(captor.capture());

        assertEquals(captor.getValue().getUsername(), "noam");
        assertTrue(captor.getValue().getCustomData().containsKey("key"));
        assertEquals(captor.getValue().getCustomData().get("key").getValue(), "regularValue");
        assertTrue(captor.getValue().getCustomData().containsKey("key_shash"));
        assertEquals(captor.getValue().getCustomData().get("key_shash").getValue().length(), 6);
        assertEquals(captor.getValue().getCustomData().get("apiKey").getValue(), "encrypt");
        assertEquals(captor.getValue().getCustomData().get("apiKey_shash").getValue().length(), 6);
        assertEquals(captor.getValue().getCustomData().get("apiKey_shash").getValue(),
                UserPropertiesSearchHelper.getSearchableProp("apiKey", "encrypt").getRight());
    }

}