package org.artifactory.version.converter.v213;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Uriah Levy
 * Validates the allowUserToAccessProfile field is added to the crowdSettings
 */
public class AllowCrowdUsersToAccessProfilePageConverterTest extends XmlConverterTest {

    private static final String CONFIG_XML_WITHOUT_PROFILE =
            "/config/test/config.2.1.1.w_crowd_wo_allowUsersToAccessProfile.xml";

    private final AllowCrowdUsersToAccessProfilePageConverter converter = new AllowCrowdUsersToAccessProfilePageConverter();

    @Test
    public void convertWithoutPreviousData() throws Exception {
        Document document = convertXml(CONFIG_XML_WITHOUT_PROFILE, converter);
        validateXml(document);
    }

    private void validateXml(Document document) {
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        Element crowdSettings = security.getChild("crowdSettings", ns);
        Element allowUsersToAccessProfile = crowdSettings.getChild("allowUserToAccessProfile", ns);
        Assert.assertNotNull(allowUsersToAccessProfile);
        Assert.assertTrue(allowUsersToAccessProfile.getText().equals("false"));
    }

}