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

package org.artifactory.ui.rest.service.admin.security.oauth;

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.md.Properties;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Gidi Shabat
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetOAuthTokensForUser implements RestService {
    private static final Logger log = LoggerFactory.getLogger(DeleteOAuthProviderSettings.class);

    @Autowired
    UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        UserInfo userInfo = userGroupService.currentUser();
        if(userInfo==null){
            String msg = "Invalid request, only authenticated user can call this service";
            log.debug(msg);
            response.error(msg);
            return;
        }
        log.debug("Retrieving OAuth tokens for user: '{}'",userInfo.getUsername());
        List<String>result= Lists.newArrayList();
        Properties properties = userGroupService.findPropertiesForUser(userInfo.getUsername());
        Multiset<String> keys = properties.keys();
        for (String key : keys) {
            result.add(key.replace("authinfo.",""));
        }
        response.iModelList(result);
    }
}
