package org.artifactory.logging.version.v10;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link LogbackRemoveZipLogsConverter}.
 *
 * @author Yossi Shaul
 */
@Test
public class LogbackRemoveZipLogsConverterTest extends XmlConverterTest {

    public void addAppendersAndLoggers() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v10/before_event_migration_logback.xml",
                new LogbackRemoveZipLogsConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        assertNoAppenderWithZipExists(root, ns);
    }

    private void assertNoAppenderWithZipExists(Element root, Namespace ns) {
        List<Element> appenders = root.getChildren("appender", ns);
        List<Element> rollingAppenders = appenders.stream().filter(a -> a.getChild("rollingPolicy", ns) != null)
                .collect(Collectors.toList());

        // explicitly verify the first appender
        Element artiFileAppender = rollingAppenders.get(0);
        String pattern = artiFileAppender.getChild("rollingPolicy", ns).getChild("FileNamePattern", ns).getText();
        assertEquals(pattern, "${artifactory.home}/logs/artifactory.%i.log");

        // make sure no rolling appender contains log.zip
        assertTrue(rollingAppenders.stream().noneMatch(
                a -> a.getChild("rollingPolicy", ns).getChild("FileNamePattern", ns).getText().endsWith(".zip")));
    }
}