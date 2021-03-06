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

package org.artifactory.ui.rest.service.admin.configuration.layouts.validation;

import org.apache.commons.lang.StringUtils;
import org.artifactory.exception.ValidationException;
import org.artifactory.util.RepoLayoutUtils;

/**
 *
 *
 * @author Lior Hasson
 */
public class ReservedLayoutNameValidator {
    public static void onValidate(String validatable) throws ValidationException {
        if (StringUtils.isNotBlank(validatable)) {
            if (RepoLayoutUtils.isReservedName(validatable)) {
                throw new ValidationException("The layout name '" + validatable + "' is reserved.");
            }
        }
    }
}
