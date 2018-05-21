/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.config;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.Async;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.spring.ReloadableBean;

import java.io.InputStream;

/**
 * @author freds
 * @date Oct 27, 2008
 */
public interface InternalCentralConfigService extends CentralConfigService, ReloadableBean {

    /**
     * Calls context.reload() after changes were made to the descriptor either by saving it (current node) or
     * receiving an HA message that it was changed.
     */
    @Async
    void callReload(CentralConfigDescriptor oldDescriptor);

    /**
     * Check whether the current state allows saving the descriptor
     * (e.g. intermediate state during upgrade does not allow)
     */
    boolean isSaveDescriptorAllowed();
}
