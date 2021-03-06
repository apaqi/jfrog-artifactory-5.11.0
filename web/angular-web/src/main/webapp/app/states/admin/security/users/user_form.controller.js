import ACTIONS from "../../../../constants/user_actions.constants";
import TOOLTIP from "../../../../constants/artifact_tooltip.constant";
import MESSAGES from "../../../../constants/configuration_messages.constants";

export class AdminSecurityUserFormController {
    constructor($scope, $state, $stateParams, $timeout, $q, $http, RESOURCE, JFrogGridFactory, UserDao, GroupsDao, GroupPermissionsDao, AdminSecurityGeneralDao, User,
                uiGridConstants, commonGridColumns, ArtifactoryModelSaver, RepositoriesDao, UserProfileDao, JFrogModal, JFrogNotifications,ArtifactoryState) {

        this.$scope = $scope;
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.$timeout = $timeout;
        this.$q = $q;
        this.$http = $http;
        this.RESOURCE = RESOURCE;
        this.User = User;
        this.repositoriesDao = RepositoriesDao;
        this.adminSecurityGeneralDao = AdminSecurityGeneralDao;
        this.modal = JFrogModal;
        this.userDao = UserDao.getInstance();
        this.groupsDao = GroupsDao.getInstance();
        this.groupPermissionsDao = GroupPermissionsDao.getInstance();
        this.artifactoryGridFactory = JFrogGridFactory;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['userdata', 'input'], ['locked']);
        this.permissionsGridOptions = {};
        this.userProfileDao = UserProfileDao;
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.artifactoryNotifications = JFrogNotifications;
        this.TOOLTIP = TOOLTIP.admin.security.usersForm;
        this.MESSAGES = MESSAGES.admin.security.users.userForm;
        this.input = {};
        this.artifactoryState = ArtifactoryState;
        this.emailChanged=false;
        this.setDisabledChecked = true;
        this.passwordRank = 0;
        this.lastSavedUserSettings = {};

        this._getPasswordExpirationState();

        if ($stateParams.username) {
            this.mode = 'edit';
            this.username = $stateParams.username;
            this.title = 'Edit ' + this.username + ' User';
            this._getUserData();

        }
        else {
            this.mode = 'create';
            this.title = 'Add New User';
            this.userdata = {
                groups: [],
                profileUpdatable: true,
                disableUIAccess: false,
                internalPasswordDisabled: false
            };
            this.saveCheckboxes();
        }
        this._createGrid();
        this._getAllRepos();
        this._getAllGroups();

    }

    userIsEffectiveAdmin(){
        return (this.userdata.admin || this.isInAdminGroup);
    }

    _getAllRepos() {
        this.reposData = {};
        this.repositoriesDao.getRepositories({type:'local'}).$promise
                .then((data) => {
                    this.reposData.locals = _.map(data,(r)=>{return r.repoKey;});
                });
        this.repositoriesDao.getRepositories({type:'remote'}).$promise
                .then((data) => {
                    this.reposData.remotes = _.map(data,(r)=>{return r.repoKey;});
                });
    }

    _createGrid() {
        this.permissionsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getPermissionCloumns())
            .setRowTemplate('default');

    }

    _getAllGroups() {
        this.userDao.getAllGroups().$promise.then((data)=> {
            this.groupsData = data;
            this.groupsList = _.map(this.groupsData, (group)=> {
                if (group.autoJoin && this.mode === 'create') {
                    this.userdata.groups.push(group.groupName);
                    this.ArtifactoryModelSaver.save();
                }
                return group.groupName;
            });
            if (this.mode === 'create') {
                this._getGroupsPermissions();
            }

            this.groupDndTemplate = `<div style="min-width:200px">
                                        {{getItemInfo().text}}
                                        <i ng-if="userScope.UserForm.groupIsAdmin(getItemInfo().text)" 
                                           jf-tooltip="Admin Privileges"
                                           class="icon icon-admin-new pull-left"></i>
                                        <i ng-if="!userScope.UserForm.groupIsAdmin(getItemInfo().text)" 
                                            class="icon icon-blank pull-left"></i>
                                    </div>`
        });
    }

    _getUserPermissions() {
        this.userDao.getPermissions({userOnly: true}, {name: this.username}).$promise.then((data)=> {
            this.userPermissions = data;
            this._setGridData();
        });
    }

    _getGroupsPermissions() {
        if (!this.userdata) return;
        if (!this.userdata.groups || !this.userdata.groups.length) {
            this.groupsPermissions = [];
            if (this.mode==='create') {
                this.permissionsGridOptions.setGridData(this.groupsPermissions);
            } else if (this.userPermissions) {
                this._setGridData();
            }
        } else {
            this.groupPermissionsDao.get({groups: this.userdata.groups}).$promise.then((data)=> {
                this.groupsPermissions = data;
                if (this.mode === 'create') {
                    this.permissionsGridOptions.setGridData(this.groupsPermissions);
                }
                else if (this.userPermissions) {
                    this._setGridData();
                }
            });
        }
    }

    _setGridData() {
        let data = [];
        if (this.groupsPermissions) {
          data = data.concat(this.groupsPermissions);
        }
        if (this.userPermissions) {
            data = data.concat(this.userPermissions);
        }
        this._fixDataFormat(data).then((fixedData)=>{
            this.permissionsGridOptions.setGridData(fixedData);
        });
    }

    _fixDataFormat(data,defer = null) {
        defer = defer || this.$q.defer();
        if (this.reposData.locals && this.reposData.remotes) {
            data.forEach((record)=>{
                if (record.repoKeys.length === 1 && record.repoKeys[0] === 'ANY LOCAL') {
                    record.repoKeysView = 'ANY LOCAL';
                    record.reposList = angular.copy(this.reposData.locals);
                }
                else if (record.repoKeys.length === 1 && record.repoKeys[0] === 'ANY REMOTE') {
                    record.repoKeysView = 'ANY REMOTE';
                    record.reposList = angular.copy(this.reposData.remotes);
                }
                else if (record.repoKeys.length === 1 && record.repoKeys[0] === 'ANY') {
                    record.repoKeysView = 'ANY';
                    record.reposList = angular.copy(this.reposData.remotes).concat(this.reposData.locals);
                }
                else {
                    record.repoKeysView = record.repoKeys.join(', ');
                    record.reposList = angular.copy(record.repoKeys);
                }
            });
            defer.resolve(data);
        }
        else {
            this.$timeout(()=>{
                this._fixDataFormat(data,defer);
            })
        }
        return defer.promise;
    }

    _getUserData() {
        this._getUserPermissions();
        this.userDao.getSingle({name: this.username}).$promise.then((data) => {
            this.userdata = data.data;
            this.isInAdminGroup = this.userdata.groupAdmin;

            if (this.userdata.internalPasswordDisabled) {
                this.passwordOriginalyDisabled = true;
            }
            if (!this.userdata.groups) {
                this.userdata.groups = [];
            } else {
                this._getGroupsPermissions();
            }
            this.saveCheckboxes();
            this.ArtifactoryModelSaver.save();
        });
        this._getApiKeyState();
    }

    _getApiKeyState() {
/*
        this.$http.head(this.RESOURCE.USER_API_KEY + '/' + this.username)
                .then(() => {
                    console.log('API KEY exists !')
                })
                .catch(() => {
                    console.log('API KEY does not exists !')
                })
*/

        this.userProfileDao.hasApiKey({},{username: this.username}).$promise.then((res)=>{
            this.apiKeyExist = true;
        }).catch(() => {
            this.apiKeyExist = false;
        })
    }

    _fixGroups(userdata) {
        let groups = userdata.groups;
        let groupsObjects = [];
        groups.forEach((group)=> {
            let realm = _.findWhere(this.groupsData, {groupName: group}).realm;
            groupsObjects.push({groupName: group, realm: realm});
        });
        delete(userdata.groups);
        userdata.userGroups = groupsObjects;
    }

    onEmailChanged() {
        this.emailChanged = true;
    }

    updateUser() {
        let payload = angular.copy(this.userdata);
        _.extend(payload, this.input);
        this._fixGroups(payload);
        this.userDao.update({name: this.userdata.name}, payload).$promise.then((data) => {
            this.savePending = false;
            if (this.userdata.name === this.User.currentUser.name) {
                if(this.emailChanged) {
                    this.artifactoryState.removeState('setMeUpUserData');
                }
                this.User.reload();
            }
            this.ArtifactoryModelSaver.save();
            this.$state.go('^.users');
        }).catch(()=>this.savePending = false);
    }

    createNewUser() {
        let payload = angular.copy(this.userdata);
        _.extend(payload, this.input);
        this._fixGroups(payload);
        this.userDao.create(payload).$promise.then((data) => {
            this.savePending = false;
            this.ArtifactoryModelSaver.save();
            this.$state.go('^.users');
        }).catch(()=>this.savePending = false);
    }

    save() {
        if (this.savePending) return;

        this.savePending = true;

        if (this.mode == 'edit')
            this.updateUser();
        if (this.mode == 'create')
            this.createNewUser();
    }

    cancel() {
        this.$state.go('^.users');
    }

    deleteUser() {
        let json = {userNames:[this.username]};
        this.modal.confirm(`Are you sure you want to delete user '${this.username}?'`)
            .then(() => this.userDao.delete(json).$promise.then(()=>this.cancel()));
    }

    userIsInAdminGroup(){
        let groups = this.userdata.groups;
        let groupsData = this.groupsData;
        for(let i in groups){
            for(let j = 0; j < groupsData.length; j ++){
                 if(groupsData[j].name === groups[i]
                         && groupsData[j].adminPrivileges){
                    return true;
                 }
            }
        }
        return false;
    }

    groupIsAdmin(group){
        return _.find(this.groupsData, {groupName: group, adminPrivileges:true});
    }

    onCheckboxChanged(){
        this.saveCheckboxes();
    }

    saveCheckboxes(){
        this.lastSavedUserSettings = {
            disableUIAccess: this.userdata.disableUIAccess,
            profileUpdatable: this.userdata.profileUpdatable,
            internalPasswordDisabled: this.userdata.internalPasswordDisabled,
        };
    }
    onChangeGroups() {
        this.userPermissions = undefined;
        this.groupsPermissions = undefined;
        this._getGroupsPermissions();
        if (this.mode === 'edit') this._getUserPermissions();
        this.$timeout(()=>{
            this.isInAdminGroup = this.userIsInAdminGroup();
            // Save last user settings before making changes
            if(this.isInAdminGroup || this.userdata.admin){
                this.userdata.disableUIAccess = false;
                this.userdata.profileUpdatable = true;
                this.userdata.internalPasswordDisabled = false;
            } else {
                this.userdata.disableUIAccess = this.lastSavedUserSettings.disableUIAccess;
                this.userdata.profileUpdatable = this.lastSavedUserSettings.profileUpdatable;
                this.userdata.internalPasswordDisabled = this.lastSavedUserSettings.internalPasswordDisabled;
            }
        });
    }

    onClickAdmin() {
        if (this.userdata.admin) {
            this.userdata.profileUpdatable = true;
            this.userdata.internalPasswordDisabled = false;
            this.userdata.disableUIAccess = false;
        }
    }

    _getPermissionCloumns() {

        let nameCellTemplate = '<div class="ui-grid-cell-contents"><a href ui-sref="admin.security.permissions.edit({permission: row.entity.permissionName})">{{row.entity.permissionName}}</a></div>';

        return [
            {
                field: "permissionName",
                name: "Permission Target",
                displayName: "Permission Target",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: nameCellTemplate,
                width:'16%'
            },
            {
                field: "effectivePermission.principal",
                name: "Applied To",
                displayName: "Applied To",
                width: '13%'
            },
            {
                field: "repoKeys",
                name: "Repositories",
                displayName: "Repositories",
                cellTemplate: this.commonGridColumns.listableColumn('row.entity.reposList','row.entity.permissionName','row.entity.repoKeysView',true),
                width:'16%'

            },
            {
                field: "effectivePermission.managed",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.managed'),
                name: "Manage",
                displayName: "Manage",
                width:'9%'
            },
            {
                field: "effectivePermission.delete",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.delete'),
                name: "Delete/Overwrite",
                displayName: "Delete/Overwrite",
                width:'15%'
            },
            {
                field: "effectivePermission.deploy",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.deploy'),
                name: "Deploy/Cache",
                displayName: "Deploy/Cache",
                width:'14%'
            },
            {
                field: "effectivePermission.annotate",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.annotate'),
                name: "Annotate",
                displayName: "Annotate",
                width:'9%'
            },
            {
                field: "effectivePermission.read",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.effectivePermission.read'),
                name: "Read",
                displayName: "Read",
                width:'8%'
            }
        ]

    }

    isSaveDisabled() {
        return this.savePending || !this.userForm || this.userForm.$invalid || ((this.input.password || this.input.retypePassword) && (this.input.password !== this.input.retypePassword));
    }

    checkPwdMatch(retypeVal) {
        return !retypeVal || (retypeVal && this.input.password === retypeVal);
    }

    isAnonymous() {
        return this.userdata.name === 'anonymous';
    }

    revokeApiKey() {
        this.modal.confirm(`Are you sure you want to revoke API key for this user ?`)
                .then(() => {
                    this.userProfileDao.revokeApiKey({}, {username: this.username}).$promise.then(()=>{
                        this.apiKeyExist = false;
                    });

                });
    }

    unlockUser() {
        this.adminSecurityGeneralDao.unlockUsers({},[this.username]).$promise.then((res)=>{
            if(res.status === 200) {
                this.userdata.locked = false;
            }
        });
    }


    expirePassword() {
        this.modal.confirm(`Are you sure you want to expire this user's password?`)
            .then(() => {
                this.userDao.expirePassword({}, {username: this.username}).$promise.then(()=> {
                    this._getUserData();
                })
            });
    }

    unexpirePassword() {
        this.userDao.unExpirePassword({}, {username: this.username}).$promise.then(()=> {
            this._getUserData();
        })
    }

    _getPasswordExpirationState() {
        this.adminSecurityGeneralDao.get().$promise.then((data) => {
            this.passwordExpirationEnabled = data.passwordSettings.expirationPolicy.enabled;
            this.userLockEnabled = data.userLockPolicy.enabled;
        });
    }

    revokeApiKey() {
        this.modal.confirm(`Are you sure you want to revoke API key for user '${this.username}'?`)
            .then(() => {
                this.userProfileDao.revokeApiKey({}, {username: this.username}).$promise.then(()=>{
                    this._getApiKeyState();
                });
            });
    }

    clearPasswordFields() {
        delete this.input.password;
        delete this.input.retypePassword;
    }

    onChangePasswordDisabled() {
        if (this.userdata.internalPasswordDisabled) {
            this.changePassword = false;
            this.clearPasswordFields();
            this.passwordReEnabled = false;
        }
        else {
            if (this.passwordOriginalyDisabled) {
                this.passwordReEnabled = true;
            }
        }
        this.onCheckboxChanged();
    }

    initActions(actionsController) {

        this.actionsController = actionsController;
        actionsController.setActionsDictionary(ACTIONS);
        actionsController.setActions([
            {
                name:'RevokeApiKey',
                visibleWhen: () => this.userdata && this.apiKeyExist && this.userdata.name !== 'anonymous',
                action: ()=>this.revokeApiKey()
            },
            {
                name:'UnlockUser',
                visibleWhen: () => this.userdata && this.userdata.locked && this.userdata.name !== 'anonymous',
                action: ()=>this.unlockUser()
            },
            {
                name:'ExpirePassword',
                visibleWhen: () => this.userdata && this.passwordExpirationEnabled && this.mode==='edit' && !this.userdata.credentialsExpired && (this.userdata.realm === 'internal' || !this.userdata.realm)  && this.userdata.name !== 'anonymous',
                action: ()=>this.expirePassword()
            },
            {
                name:'UnexpirePassword',
                visibleWhen: () => this.userdata && this.userdata.credentialsExpired && !this.userdata.locked  && this.userdata.name !== 'anonymous',
                action: ()=>this.unexpirePassword()
            },
            {
                name:'DeleteUser',
                visibleWhen: () => this.mode === 'edit' && this.userdata && this.userdata.name !== 'anonymous',
                action: ()=>this.deleteUser()
            }
        ]);

    }

    // Validations
    checkUserName(value) {
        return !(/[A-Z]/.test(value));
    }

}