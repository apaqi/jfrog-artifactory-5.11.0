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

package org.artifactory.ui.rest.service.admin.security.auth.currentuser;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.ui.rest.model.admin.security.general.SecurityConfig;
import org.artifactory.ui.rest.model.admin.security.user.BaseUser;
import org.artifactory.ui.rest.service.admin.security.general.GetSecurityConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetCurrentUserService implements RestService {

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private GetSecurityConfigService getSecurityConfigService;

    @Autowired
    private SecurityService securityService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean proWithoutLicense = false;
        if (!(addonsManager instanceof OssAddonsManager) && !addonsManager.isLicenseInstalled()) {
            proWithoutLicense = true;
        }
        boolean offlineMode=true;
        CentralConfigDescriptor descriptor = ContextHelper.get().getCentralConfig().getDescriptor();
        if (ConstantValues.versionQueryEnabled.getBoolean() && !descriptor.isOfflineMode()) {
            offlineMode=false;
        }
        getSecurityConfigService.execute(request, response);
        SecurityConfig securityConfig = (SecurityConfig) response.getIModel();
        boolean canManage = authService.hasPermission(ArtifactoryPermission.MANAGE);
        BaseUser baseUser = new BaseUser(authService.currentUsername(), authService.isAdmin());
        baseUser.setCanDeploy(authService.canDeployToLocalRepository());
        baseUser.setCanManage(canManage || proWithoutLicense);
        baseUser.setProfileUpdatable(authService.isUpdatableProfile());
        baseUser.setPreventAnonAccessBuild(securityConfig.isAnonAccessToBuildInfosDisabled());
        // The name misleading, it is not Pro, it is NON-OSS without license (can be HA etc..)
        baseUser.setProWithoutLicense(proWithoutLicense);
        baseUser.setAnonAccessEnabled(securityConfig.isAnonAccessEnabled());
        baseUser.setRequireProfileUnlock(authService.requireProfileUnlock());
        baseUser.setRequireProfilePassword(authService.requireProfilePassword());
        baseUser.setOfflineMode(offlineMode);
        baseUser.setExistsInDB(!authService.isTransientUser());

        if (!StringUtils.isBlank(authService.currentUsername())) {
            Integer userPasswordDaysLeft = securityService.getUserPasswordDaysLeft(authService.currentUsername());
            if (userPasswordDaysLeft != null) {
                baseUser.setCurrentPasswordValidFor(userPasswordDaysLeft);
            }
        }

        response.iModel(baseUser);
    }
}
