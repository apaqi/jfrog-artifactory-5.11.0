package org.artifactory.log.logback;

import org.apache.commons.lang3.StringUtils;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.common.ArtifactoryHome;
import org.jfrog.common.logging.logback.LogbackContextConfigurator;
import org.jfrog.common.logging.logback.servlet.LoggerConfigInfo;

import java.util.Arrays;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

/**
 * @author Yinon Avraham.
 */
public class ArtifactoryLoggerConfigInfo extends LoggerConfigInfo {

    private final ArtifactoryHome artifactoryHome;

    public ArtifactoryLoggerConfigInfo(String contextId, ArtifactoryHome artifactoryHome) {
        super(contextId, artifactoryHome.getLogbackConfig());
        this.artifactoryHome = artifactoryHome;
    }

    @Override
    protected void configure(LogbackContextConfigurator configurator) {
        super.configure(configurator);
        configurator
                .property(ArtifactoryContext.CONTEXT_ID_PROP, normalizedContextId())
                .property(ArtifactoryHome.SYS_PROP, artifactoryHome.getHomeDir().getAbsolutePath());

        // add custom logback properties to be used by logback (pattern/file names) if defined for testing
        String customProps = artifactoryHome.getArtifactoryProperties()
                .getProperty("artifactory.test.logback.custom", "");
        if (StringUtils.isNotBlank(customProps)) {
            String[] customParams = customProps.split(",");
            Arrays.stream(customParams).forEach(
                    p -> configurator.property(p.split("::")[0], p.split("::")[1])
            );
        }
    }

    private String normalizedContextId() {
        String contextId = trimToEmpty(getContextId());
        contextId = "artifactory".equalsIgnoreCase(contextId) ? "" : contextId + " ";
        return isBlank(contextId) ? "" : contextId.toLowerCase();
    }
}
