package org.artifactory.logging.version.v10;

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Remove zip functionality from rolling appenders.
 *
 * @author Yossi Shaul
 */
public class LogbackRemoveZipLogsConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LogbackRemoveZipLogsConverter.class);

    @Override
    public void convert(Document doc) {
        log.debug("Starting logback conversion --> Removing zip from rolling appenders.");
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        List<Element> appenders = root.getChildren("appender", ns);
        appenders.forEach(a -> removeZip(a, ns));
        log.debug("Migration logs conversion completed.");
    }

    private void removeZip(Element a, Namespace ns) {
        Element rollingPolicy = a.getChild("rollingPolicy", ns);
        if (rollingPolicy != null) {
            Element fileNamePattern = rollingPolicy.getChild("FileNamePattern", ns);
            if (fileNamePattern != null) {
                String pattern = fileNamePattern.getText();
                if (pattern.endsWith(".zip")) {
                    fileNamePattern.setText(StringUtils.removeEnd(pattern, ".zip"));
                }
            }
        }
    }
}
