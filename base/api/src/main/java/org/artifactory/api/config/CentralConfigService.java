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

package org.artifactory.api.config;

import org.apache.commons.lang3.builder.DiffResult;
import org.artifactory.descriptor.DescriptorAware;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.jfrog.common.config.diff.DataDiff;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * User: freds Date: Aug 3, 2008 Time: 6:22:39 PM
 */
public interface CentralConfigService extends DescriptorAware<CentralConfigDescriptor>, ImportableExportable {
    DateTimeFormatter getDateFormatter();

    void saveEditedDescriptorAndReload(MutableCentralConfigDescriptor descriptor);

    /**
     * Reload configuration from storage
     *
     * @return {@code true} if succeeded reloading
     */
    void reloadConfiguration();

    void reloadConfigurationLazy();

    String getServerName();

    /**
     * Formats the given milliseconds date with the date format configured in the central config.
     *
     * @param date Date in milliseconds
     * @return Formatted date string
     */
    String format(long date);

    VersionInfo getVersionInfo();

    /**
     * @return A mutable COPY of the central config descriptor.
     */
    MutableCentralConfigDescriptor getMutableDescriptor();

    String getConfigXml();

    void setConfigXml(String xmlConfig);

    void setConfigXml(String xmlConfig, boolean ignoreValidation);

    void setLogo(File logo) throws IOException;

    boolean defaultProxyDefined();

    DiffResult findDiff(CentralConfigDescriptor oldDescriptor, CentralConfigDescriptor newDescriptor);

    void mergeAndSaveNewData(Collection<DataDiff<?>> diffs);
}