package org.artifactory.state;

import org.artifactory.config.CentralConfigKey;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.security.access.AccessService;
import org.artifactory.spring.Reloadable;
import org.artifactory.state.model.StateInitializer;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Shay Bagants
 */
@Service
@Reloadable(beanClass = StateInitializer.class, initAfter = {InternalCentralConfigService.class, AccessService.class},
        listenOn = CentralConfigKey.none)
public class StateInitializerImpl implements StateInitializer {

    @Override
    public void init() {

    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {

    }

    @Override
    public String getSupportBundleDump() {
        return null;
    }
}
