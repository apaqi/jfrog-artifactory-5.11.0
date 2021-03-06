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

package org.artifactory.util.layouts.token;

import org.apache.commons.lang.StringUtils;

/**
 * @author Noam Y. Tenne
 */
public class OrganizationPathTokenFilter extends BaseTokenFilter {

    private static OrganizationPathTokenFilter INSTANCE = new OrganizationPathTokenFilter();

    private OrganizationPathTokenFilter() {
    }

    public static OrganizationPathTokenFilter getInstance() {
        return INSTANCE;
    }

    @Override
    public String forPath(String tokenValue) {
        return StringUtils.replaceChars(tokenValue, '.', '/');
    }

    @Override
    public String fromPath(String tokenValue) {
        return StringUtils.replaceChars(tokenValue, '/', '.');
    }
}
