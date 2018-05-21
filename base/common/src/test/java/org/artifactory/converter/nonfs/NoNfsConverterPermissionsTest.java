package org.artifactory.converter.nonfs;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.artifactory.common.ArtifactoryConfigurationAdapter;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.converter.ConverterPreconditionException;
import org.artifactory.converter.ConvertersManagerImpl;
import org.artifactory.converter.VersionProviderImpl;
import org.artifactory.converter.helpers.ConvertersManagerTestBase;
import org.artifactory.converter.helpers.MockArtifactoryHome;
import org.artifactory.test.TestUtils;
import org.jfrog.config.ConfigurationManager;
import org.jfrog.config.wrappers.ConfigurationManagerImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.artifactory.version.ArtifactoryVersion.v4111;

/**
 * Test for NoNfsFilePermissionChecker
 * makes sure that without permissions there will be no conversions
 *
 * @author nadavy
 */
@Test
public class NoNfsConverterPermissionsTest extends ConvertersManagerTestBase {

    private File home;
    private File haEtcDir;
    private File etcDir;
    private File pluginsDir;

    @BeforeClass
    public void init() throws IOException {
        home = TestUtils.createTempDir(getClass());
        createHomeEnvironment(home, v4111);
        File artHaDir = new File(home, ".artifactory-ha");
        haEtcDir = new File(artHaDir, "ha-etc");
        FileUtils.forceMkdir(haEtcDir);
        File artDir = new File(home, ".artifactory");
        etcDir = new File(artDir, "etc");
        pluginsDir = new File(etcDir, "plugins");
    }


    @Test(dataProvider = "blockedFiles", expectedExceptions = ConverterPreconditionException.class)
    public void convertProperties(File... blockedFiles) throws IOException {
        ArtifactoryHome artifactoryHome = new MockArtifactoryHome(home);
        // Create Full Ha environment
        createHaEnvironment(home);
        for (File blockedFile : blockedFiles) {
            FileUtils.forceMkdir(blockedFile.getParentFile());
            Files.touch(blockedFile);
            blockedFile.setReadable(false);
            blockedFile.setWritable(false);
            if (SystemUtils.IS_OS_WINDOWS && blockedFile.isDirectory() && blockedFile.canWrite()) {
                // on some windows machines setting permissions on folders doesn't work -> skip
                throw new ConverterPreconditionException("skipping dir " + blockedFile + " on windows");
            }
        }
        try {
            assertConversionBlocked(artifactoryHome);
        } finally {
            for (File blockedFile : blockedFiles) {
                blockedFile.setReadable(true);
                blockedFile.setWritable(true);
            }
        }
    }

    @DataProvider
    public Object[][] blockedFiles() {
        return new Object[][]{
                {new File(haEtcDir, "storage.properties")},
                {new File(etcDir, "security/artifactory.ssh.private"), new File(etcDir,
                        "security/artifactory.ssh.public")},
                {new File(haEtcDir, "storage.properties")},
                {new File(pluginsDir, "plugin.groovy")},
                {new File(haEtcDir, "ui")},
                {new File(haEtcDir, "artifactory.system.properties")},
                {new File(etcDir, "binarystore.xml")},
        };
    }

    private void assertConversionBlocked(ArtifactoryHome artifactoryHome) {
        ConfigurationManager configurationManager = null;
        try {
            ArtifactoryHome.bind(artifactoryHome);
            VersionProviderImpl vp = new VersionProviderImpl(artifactoryHome);
            vp.initOriginalHomeVersion();
            ArtifactoryConfigurationAdapter adapter = new ArtifactoryConfigurationAdapter(artifactoryHome);
            configurationManager = ConfigurationManagerImpl.create(adapter);
            ConvertersManagerImpl convertersManager = new ConvertersManagerImpl(artifactoryHome, vp, configurationManager);
            initVpDbVersion(vp);
            convertersManager.assertPreInitNoConversionBlock();
            convertersManager.assertPostInitNoConversionBlock();
        } finally {
            Optional.ofNullable(configurationManager).ifPresent(ConfigurationManager::destroy);
            ArtifactoryHome.unbind();
        }
    }

    private void initVpDbVersion(VersionProviderImpl vp) {
        Class<? extends VersionProviderImpl> aClass = vp.getClass();
        try {
            Field field = aClass.getDeclaredField("originalHomeVersion");
            field.setAccessible(true);
            Object o = field.get(vp);
            Field field2 = aClass.getDeclaredField("originalDbVersion");
            field2.setAccessible(true);
            field2.set(vp, o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createHaEnvironment(File home) throws IOException {
        createHaPluginsFile(home);
        createHaUiFile(home);
        createHaBackupFile(home);
        createArtifactorySystemPropertiesFile(home);
        createBinaryStoreXmlFile(home);
        createMimeTypeXmlFile(home);
        createStoragePropertiesXmlFile(home);
        createSshKeys(home);
    }
}
