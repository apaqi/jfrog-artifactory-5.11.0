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

package org.artifactory.ui.rest.service.admin.security.user;

import org.artifactory.api.security.SecurityService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.exceptions.PasswordExpireException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;

/**
 * Service expiring user password
 *
 * Created by Michael Pasternak
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExpireUserPasswordService<T extends String> implements RestService<T> {

    @Autowired
    protected SecurityService securityService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        String userName = request.getImodel();
        try {
            securityService.expireUserCredentials(userName);
            response.responseCode(Response.Status.OK.getStatusCode());
            response.info("User credentials were successfully expired");
        } catch (PasswordExpireException e) {
            response.responseCode(Response.Status.BAD_REQUEST.getStatusCode());
            response.error(e.getMessage());
        }
    }
}
