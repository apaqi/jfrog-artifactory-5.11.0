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

package org.artifactory.ui.rest.service.admin.security.ldap.ldapsettings;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.StatusEntryLevel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapSettingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestLdapSettingsService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    SecurityService securityService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        LdapSettingModel ldapSetting = (LdapSettingModel) request.getImodel();
        String testUsername = ldapSetting.getTestUsername();
        String testPassword = ldapSetting.getTestPassword();
        // validate if user name and password exist
        if (testUsername == null || testPassword == null) {
            response.error("Please enter test username and password to test the LDAP settings");
            return;
        }
        // test ldap connection
        testLdapConnection(response, ldapSetting, testUsername, testPassword);
    }

    /**
     * test ldap connection
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param ldapSetting         - ldap setting model
     * @param testUsername        - test user name
     * @param testPassword        - test password
     */
    private void testLdapConnection(RestResponse artifactoryResponse, LdapSettingModel ldapSetting, String testUsername,
            String testPassword) {
        BasicStatusHolder status = securityService.testLdapConnection(ldapSetting, testUsername, testPassword);
        List<StatusEntry> infos = status.getEntries(StatusEntryLevel.INFO);
        if (status.isError()) {
            artifactoryResponse.error(status.getStatusMsg());
            for (StatusEntry info : infos) {
                artifactoryResponse.info(info.getMessage());
            }
            List<StatusEntry> warnings = status.getEntries(StatusEntryLevel.WARNING);
            for (StatusEntry warning : warnings) {
                artifactoryResponse.warn(warning.getMessage());
            }
        } else {
            artifactoryResponse.info("Successfully connected and authenticated the test user");
        }
    }
}
