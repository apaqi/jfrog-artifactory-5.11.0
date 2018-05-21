export class BundlePageController {
    constructor($scope, $filter, $stateParams, $location, BundlesDao, JFrogUIUtils) {

    	this.$scope = $scope;
    	this.$filter = $filter;
        this.$stateParams = $stateParams;
        this.BundleDao = BundlesDao;
        this.$location = $location;
        this.JFrogUIUtils = JFrogUIUtils;

        this._getBundleVersions();
	    this.fileInfo = [];
	    this.activeItem = '';
    }

    _getBundleVersions() {
        this.BundleDao.getBundleVersions({
            name: this.$stateParams.bundleName
        }).$promise.then((data) => {
	        this.versions = _.sortBy(data.versions, (i) => {
	        	return i.created;
	        });

            this._getBundleData(this.$stateParams.bundleName, this.$stateParams.version)
        });
    }

    _getBundleData(name, version) {
        this.BundleDao.getBundleData({name,version}).$promise.then((data) => {
        	// Group by component name
	        this.artifactsList = _.groupBy(data.artifacts, 'component_name');
	        this.summaryColumns = this.getSummaryColumns(data);
        })
    }

	updateFileInfo(fileInfo) {
		this.activeItem = fileInfo.name;
		this.fileInfo = [
			{label: 'Name', value: fileInfo.name || ''},
			{label: 'Path', value: fileInfo.path || '', copy: true},
			{label: 'Created', value: fileInfo.created || ''},
			{label: 'Version', value: fileInfo.component_version || ''},
			{label: 'Path', value: fileInfo.path || ''},
            {label: 'Size', value: this.formatFileSize(fileInfo.size) || ''}
		]
	}


	getSummaryColumns(data) {
		return [{
			label: 'Version ID',
			class: 'name',
			template: data.version || '-',
			isActive: true
		},
		{
			label: 'Short Description',
			class: 'short-description',
			template: data.description,
			isActive: data.description && !!data.description.length
		},
		{
			label: 'Creation Date',
			class: 'creation-date',
			template: this.formatDate(data.created) || '-',
			isActive: true
		},
		{
			label: 'Size',
			class: 'size',
			template: this.formatFileSize(data.size) || '-',
			isActive: true
		}]
	}

	formatDate(date) {
		date = this.$filter('date')(date, "d MMM, yyyy");
    	return date;
	}

	formatFileSize(bytes) {
    	return this.$filter('filesize')(bytes);
	}
}