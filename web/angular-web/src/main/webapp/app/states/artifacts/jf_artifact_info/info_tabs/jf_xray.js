import EVENTS from "../../../../constants/artifacts_events.constants";
import MESSAGES from '../../../../constants/artifacts_messages.constants';
class jfXrayController {
    constructor($scope, JFrogEventBus, ArtifactXrayDao, ArtifactActionsDao, JFrogModal) {

        this.$scope = $scope;
        this.JFrogEventBus = JFrogEventBus;
        this.ArtifactXrayDao = ArtifactXrayDao;
        this.artifactActionsDao = ArtifactActionsDao;
        this.MESSAGES = MESSAGES;
        this.modal = JFrogModal;

        this._registerEvents();
        this._getXrayData();
    }

    _registerEvents() {
        let self = this;

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode !== node) {
                this.currentNode = node;
                self._getXrayData();
            }
        });
    }

    _getXrayData() {
        this.ArtifactXrayDao.getData({repoKey: this.currentNode.data.repoKey, path: this.currentNode.data.path}).$promise.then((response) => {
            this.artifactXrayData = response.data;
        });
    }

    _doAllowDownload() {
        this.modal.confirm("Download will be allowed until Xray runs another scan that generates an alert for this artifact.", 'Allow download')
                .then(() => {
                    this.artifactActionsDao.perform({action: 'allowDownload', params: 'true', repoKey: this.currentNode.data.repoKey, path: this.currentNode.data.path}).$promise.then((data) => {
                        this._getXrayData();
                    })
                });
    }

    xrayAlertMessage() {
        if (this.artifactXrayData.allowBlockedArtifacts) {
            return this.MESSAGES.xray_tab.blocked_artifact_ignored;
        }
        return this.MESSAGES.xray_tab.blocked_artifact;
    }

}

export function jfXray() {
    return {
        restrict: 'EA',
        controller: jfXrayController,
        controllerAs: 'jfXray',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_xray.html'
    }
}