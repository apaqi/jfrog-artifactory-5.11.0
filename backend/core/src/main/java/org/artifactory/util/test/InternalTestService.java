package org.artifactory.util.test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.traffic.read.TrafficReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Calendar;

/**
 * An internal service used during integration tests.
 *
 * @author Yossi Shaul
 */
@Service
public class InternalTestService {
    private static final Logger log = LoggerFactory.getLogger(InternalTestService.class);

    public LoggerContext printAndGetContext(String message) {
        message += ". home dir name " + ArtifactoryHome.get().getHomeDir().getName();
        log.info(message);
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    public ContextSelector getContextSelector() {
        return ContextSelectorStaticBinder.getSingleton().getContextSelector();
    }

    public Logger getLogger() {
        return log;
    }

    public void rotateTrafficLog() {
        //TODO: [by YS] do we really need to rotate of we just need to clean?
        File logDir = ContextHelper.get().getArtifactoryHome().getLogDir();
        TrafficReader trafficReader = new TrafficReader(logDir);
        Calendar from = Calendar.getInstance();
        from.add(Calendar.YEAR, -1);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.YEAR, 1);
        trafficReader.readFiles(from.getTime(), to.getTime()).forEach(file -> file.delete());
        ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getILoggerFactory()
                .getLogger("org.artifactory.traffic.TrafficLogger");
        RollingFileAppender file = (RollingFileAppender) log.getAppender("TRAFFIC");
        if (file != null) {
            file.rollover();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
        }
    }
}
