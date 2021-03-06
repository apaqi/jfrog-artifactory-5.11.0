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

package org.artifactory.storage.tx;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Per-session single threaded session resources (listeners) manager
 *
 * @author freds
 */
public class SessionResourceManagerImpl implements SessionResourceManager {
    private static final Logger log = LoggerFactory.getLogger(SessionResourceManagerImpl.class);

    private final Map<Class, SessionResource> resources = Maps.newHashMapWithExpectedSize(3);

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends SessionResource> T getOrCreateResource(Class<T> resourceClass) {
        T result = (T) resources.get(resourceClass);
        if (result == null) {
            try {
                result = resourceClass.newInstance();
                resources.put(resourceClass, result);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Override
    public void beforeCommit() {
        for (SessionResource resource : resources.values()) {
            resource.beforeCommit();
        }
    }

    @Override
    public void afterCompletion(boolean commit) {
        RuntimeException ex = null;
        for (SessionResource resource : resources.values()) {
            try {
                //Always call after completion on resources
                resource.afterCompletion(commit);
            } catch (RuntimeException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error while releasing resources " + resource + " : " + e.getMessage(), e);
                }
                ex = e;
            }
        }
        if (ex != null) {
            throw ex;
        }
    }
}
