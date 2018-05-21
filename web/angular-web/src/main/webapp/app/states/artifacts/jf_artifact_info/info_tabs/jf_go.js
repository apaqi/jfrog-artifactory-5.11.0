/**
 * Created by matang on 09/11/2017.
 */

import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';

class jfGoController {
    constructor($scope, $element, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.$scope = $scope;
        this.$element = $element;
        this.artifactViewsDao = ArtifactViewsDao;
        this.DICTIONARY = DICTIONARY.go;
        this.gridDependenciesOptions = {};
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.goData = {};

        this._getGoData();
        this._registerEvents();
    }


    _getGoData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "go",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise.then((data) => {
            this.goData = data;
            this._createGrid();
        });
    }

    _createGrid() {
        if (this.goData.goDependencies) {
            this.gridDependenciesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setRowTemplate('default')
                .setColumns(this._getColumns())
                .setGridData(this.goData.goDependencies)
        }
    }

    _getColumns() {
        return [
            {
                name: 'Name',
                displayName: 'Name',
                field: 'name'
            },
            {
                name: 'Version',
                displayName: 'Version',
                field: 'version'
            }]
    }


    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.currentNode = node;
            this._getGoData();
        });
    }
}
export function jfGo() {
    return {
        restrict: 'EA',
        controller: jfGoController,
        controllerAs: 'jfGo',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_go.html'
    }
}