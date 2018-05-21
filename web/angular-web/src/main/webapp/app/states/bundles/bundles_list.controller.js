export class BundlesListController {
    constructor($scope, $q, BundlesDao, JFrogTableViewOptions) {

        this.$scope = $scope;
        this.$q = $q;
        this.BundlesDao = BundlesDao;
        this.JFrogTableViewOptions = JFrogTableViewOptions;
        this.allBundlesGridOptions = {};

        this._createGrid();
        this._getBundlesData();

    }


    _getBundlesData() {
        this.BundlesDao.getData().$promise.then((results) => {
            this.allBundles = results.bundles;
            this.tableViewOptions.setData(this.allBundles)
        });
    }

    _createGrid() {
        this.tableViewOptions = new this.JFrogTableViewOptions(this.$scope);
        this.tableViewOptions.setColumns(this._getColumns())
                .setRowsPerPage(20)
                .setEmptyTableText('No bundles');
    }

    _getColumns() {
        return [
            {
                header: "Bundle Name",
                field: "bundleName",
                cellTemplate: '<div class="ui-grid-cell-contents"><a href class="no-cm-action" ui-sref="bundles.bundle_page({bundleName: row.entity.name, version: row.entity.latestVersion})">{{row.entity.name}}</a></div>',
                width: '60%'
            },
            {
                header: "Last Version",
                field: "lastVersion",
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.latestVersion}}</div>',
                width: '20%'
            },
            {
                header: "Created",
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.created | date: \'d MMMM, yyyy\'}}</div>',
                field: "created",
                width: '20%'
            }
        ]
    }
}