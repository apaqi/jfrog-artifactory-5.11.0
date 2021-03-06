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

package org.artifactory.api.common;

import org.artifactory.common.StatusEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * NOTE: WHEN CHANGING THE NAME OR THE PACKAGE OF THIS CLASS, MAKE SURE TO UPDATE TEST AND PRODUCTION LOGBACK
 * CONFIGURATION FILES WITH THE CHANGES AND CREATE A CONVERTER IF NEEDED. SOME APPENDERS DEPEND ON THIS.
 * <p/>
 * Specialized status holder for the import and export processes.
 *
 * @author Gidi Shabat
 */
public class ImportExportStatusHolder extends BasicStatusHolder {
    protected static final Logger log = LoggerFactory.getLogger(ImportExportStatusHolder.class);

    @Override
    protected void logEntry(@Nonnull StatusEntry entry, @Nonnull Logger logger) {
        if (isVerbose()) {
            doLogEntry(entry, log);
        }
        doLogEntry(entry, logger);
    }
}
