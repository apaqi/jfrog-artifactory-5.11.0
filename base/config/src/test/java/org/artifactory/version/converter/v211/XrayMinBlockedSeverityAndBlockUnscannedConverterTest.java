package org.artifactory.version.converter.v211;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Test the removal of the 'minimumBlockedSeverity' and 'blockUnscannedArtifacts'.
 * Each repo type in the descriptor which used for this test.
 *
 * @author Yuval Reches
 */
public class XrayMinBlockedSeverityAndBlockUnscannedConverterTest extends XmlConverterTest {

    private String CONFIG_XML = "/config/test/config.2.1.0.xray_configs.xml";

    @Test
    public void convert() throws Exception {
        Document document = convertXml(CONFIG_XML, new XrayMinBlockedSeverityAndBlockUnscannedConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        validateConfig(rootElement.getChild("localRepositories", namespace), namespace);
        validateConfig(rootElement.getChild("remoteRepositories", namespace), namespace);
    }

    private void validateConfig(Element repos, Namespace namespace) {
        List<Element> children = repos.getChildren();
        assertNotNull(children);
        assertFalse(children.isEmpty());

        children.forEach(repoElement -> assertConfigRemoval(repoElement, namespace, "minimumBlockedSeverity"));
        children.forEach(repoElement -> assertConfigRemoval(repoElement, namespace, "blockUnscannedArtifacts"));
    }

    private void assertConfigRemoval(Element repoElement, Namespace namespace, String name) {
        Element xray = repoElement.getChild("xray", namespace);
        if (xray != null) {
            assertNull(xray.getChild(name, namespace));
        }
    }

}
