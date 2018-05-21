package org.artifactory.logging.version.v10;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.artifactory.logging.version.v10.LogbackAddEventLogConverter.EVENT_APPENDER_NAME;
import static org.artifactory.logging.version.v10.LogbackAddEventLogConverter.EVENT_LOGGER_NAME;
import static org.testng.Assert.assertTrue;

/**
 * @author Yossi Shaul
 */
@Test
public class LogbackAddEventLogConverterTest extends XmlConverterTest {

    public void addAppendersAndLoggers() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v10/before_event_migration_logback.xml",
                new LogbackAddEventLogConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        assertAppenderExists(root, ns, EVENT_APPENDER_NAME);
        assertLoggerExists(root, ns, EVENT_LOGGER_NAME);
    }

    private void assertLoggerExists(Element root, Namespace ns, String loggerName) {
        assertTrue(root.getChildren("logger", ns).stream()
                        .anyMatch(logger -> logger.getAttributeValue("name", ns).equals(loggerName)),
                "Logger '" + loggerName + "' not found after conversion");
    }

    private void assertAppenderExists(Element root, Namespace ns, String appenderName) {
        assertTrue(root.getChildren("appender", ns).stream()
                        .anyMatch(appender -> appender.getAttributeValue("name", ns).equals(appenderName)),
                "Appender '" + appenderName + "' not found after conversion");
    }
}
