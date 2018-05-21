package org.artifactory.version.converter.v213;

import org.artifactory.version.converter.XmlConverter;
import org.artifactory.version.converter.v160.AddonsDefaultLayoutConverterHelper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Liz Dashevski
 */
public class GoDefaultLayoutConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(GoDefaultLayoutConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting the go default repository layout conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        log.debug("Adding go default repository layout");
        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        addGoDefaultLayout(repoLayoutsElement, namespace);

        log.info("Conan default repository layout conversion finished successfully");
    }

    private void addGoDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(
                AddonsDefaultLayoutConverterHelper.getRepoLayoutElement(repoLayoutsElement, namespace,
                        "go-default",
                        "[orgPath]/[module]/@v/v[refs].zip",
                        "false", null,
                        ".*",
                        ".*"));
    }
}
