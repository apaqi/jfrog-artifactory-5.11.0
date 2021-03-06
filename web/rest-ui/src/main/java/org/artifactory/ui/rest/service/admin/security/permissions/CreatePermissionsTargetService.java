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

package org.artifactory.ui.rest.service.admin.security.permissions;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AclService;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.security.AceInfo;
import org.artifactory.security.MutableAceInfo;
import org.artifactory.security.MutableAclInfo;
import org.artifactory.security.MutablePermissionTargetInfo;
import org.artifactory.ui.rest.model.admin.security.permissions.PermissionTargetModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.EffectivePermission;
import org.artifactory.util.AlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreatePermissionsTargetService extends BasePermissionsTargetService {
    private static final Logger log = LoggerFactory.getLogger(CreatePermissionsTargetService.class);
    @Autowired
    AclService aclService;
    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        PermissionTargetModel permissionTarget = (PermissionTargetModel) request.getImodel();
        try {

            // check if anny remote/local/distribution is set and filter repo keys list accordingly
            filteredRepoKey(permissionTarget);
            InfoFactory infoFactory = InfoFactoryHolder.get();
            // create new acl
            MutableAclInfo mutableAclInfo = infoFactory.createAcl();
            // update permission target data
            updatePermissionTarget(permissionTarget, infoFactory, mutableAclInfo);
            // update user and groups permissions
            updateAcesPermissions(permissionTarget, infoFactory, mutableAclInfo);
            // update acl
            aclService.createAcl(mutableAclInfo);
            response.info("Successfully Created permission target '" + permissionTarget.getName() + "'");
        }
        catch (Exception e) {
            if (e instanceof AlreadyExistsException) {
                response.error("Permission target '" + permissionTarget.getName() + "' already exists");
            } else {
                response.error("Unexpected error has occurred please review the logs");
            }
            log.debug(e.toString());
        }
    }

    /**
     * update user and groups permissions
     *
     * @param permissionTarget - permission target
     * @param infoFactory      - info factory
     * @param mutableAclInfo   - ace info new instance
     */
    private void updateAcesPermissions(PermissionTargetModel permissionTarget, InfoFactory infoFactory, MutableAclInfo mutableAclInfo) {
        Set<AceInfo> aclInfos = new HashSet<>();
        // update group permission
        permissionTarget.getGroups().forEach(permissionModel -> {
            MutableAceInfo ace = infoFactory.createAce(permissionModel.getPrincipal(), true, permissionModel.getMask());
            // update ace permissions
            updateAcePermission(permissionModel, ace);
            aclInfos.add(ace);
        });
        // update user permission
        permissionTarget.getUsers().forEach(permissionModel -> {
            MutableAceInfo ace = infoFactory.createAce(permissionModel.getPrincipal(), false, permissionModel.getMask());
            // update ace permissions
            updateAcePermission(permissionModel, ace);
            aclInfos.add(ace);
        });
        mutableAclInfo.setAces(aclInfos);
    }

    /**
     * update permission target data from model
     *
     * @param permissionTarget - model permission target data
     * @param infoFactory      - info factory (create permission and ace objects)
     * @param mutableAclInfo   - ace info new instance
     */
    private void updatePermissionTarget(PermissionTargetModel permissionTarget, InfoFactory infoFactory, MutableAclInfo mutableAclInfo) {
        MutablePermissionTargetInfo permission = infoFactory.createPermissionTarget();
        populatePermissionData(permissionTarget, permission);
        mutableAclInfo.setPermissionTarget(permission);
    }

    /**
     * update ace permissions
     *
     * @param permissionModel - permission model data
     * @param ace             - ace new instance
     */
    private void updateAcePermission(EffectivePermission permissionModel, MutableAceInfo ace) {
        ace.setAnnotate(permissionModel.isAnnotate());
        ace.setDelete(permissionModel.isDelete());
        ace.setDeploy(permissionModel.isDeploy());
        ace.setManage(permissionModel.isManaged());
        ace.setRead(permissionModel.isRead());
    }

    /**
     * populate permission data
     *
     * @param permissionTarget - permission target new instance
     * @param permission       - permission model
     */
    private void populatePermissionData(PermissionTargetModel permissionTarget, MutablePermissionTargetInfo permission) {
        permission.setExcludes(permissionTarget.getExclude());
        permission.setExcludesPattern(permissionTarget.getExcludePattern());
        permission.setIncludes(permissionTarget.getIncludes());
        permission.setIncludesPattern(permissionTarget.getIncludePattern());
        permission.setName(permissionTarget.getName());
        if (!permissionTarget.getRepoKeys().isEmpty()) {
            List<String> keys = new ArrayList<>();
            permissionTarget.getRepoKeys().forEach(repoKeyType -> {
                String repoKey = repoKeyType.getRepoKey();
                if (repoKeyType.getType().equals("remote")){
                        repoKey = repoKey+"-cache";
                        }
                keys.add(repoKey);
                    });
                    permission.setRepoKeys(keys);
        }
    }
}
