import field_options from '../../constants/field_options.constats';

export default class PackagesNativeController {
	constructor($state, $scope, PackagesDAO, $q, FooterDao, ArtifactoryFeatures, User, ArtifactoryStorage) {
		this.$state = $state;
		this.$scope = $scope;
		this.$q = $q;
		this.packagesDAO = PackagesDAO;
		this.footerDao = FooterDao;
		this.artifactoryStorage = ArtifactoryStorage;

		if (User.currentUser.name === 'anonymous' && !User.currentUser.anonAccessEnabled) {
			$state.go('login');
			return;
		}
		if (ArtifactoryFeatures.isOss()) {
			$state.go('home');
			return;
		}
	}

	getPackageTypes(daoParams) {
		let defferd = this.$q.defer();
		let pkgTypes = field_options.repoPackageTypes.map((t) => {
			return {
				text: t.value,
				displayText: t.text,
				iconClass: `icon-${t.icon}`,
				disabled: t.value !== 'docker'
			};
		});
		pkgTypes = _.sortBy(pkgTypes, pt => !!pt.disabled)
		defferd.resolve(pkgTypes);
		return defferd.promise;
	}

	getPackages(daoParams) {
		return this.packagesDAO.getPackages(this._transformDaoParams({packageType: daoParams.packageType, sort_by: daoParams.sortBy, order: daoParams.order}), daoParams.filters).$promise;
	}

	getFilters(daoParams) {
		return this.packagesDAO.getFilters(this._transformDaoParams(daoParams)).$promise;
	}

	getPackage(daoParams) {
        return this.packagesDAO.getPackage(this._transformDaoParams({packageType: daoParams.packageType, package: daoParams.package, sort_by: daoParams.sortBy, order: daoParams.order}), daoParams.repoFilter || []).$promise;
	}

	getPackageExtraInfo(daoParams) {
        return this.packagesDAO.getPackageExtraInfo(this._transformDaoParams({packageType: daoParams.packageType, package: daoParams.package, $no_spinner: true})).$promise;
	}

	getPackageDownloadsCount(daoParams) {
		return this.packagesDAO.getPackageDownloadsCount(this._transformDaoParams(daoParams)).$promise;
	}

	getVersionDownloadsCount(daoParams) {
        daoParams.$no_spinner = true;
		return this.packagesDAO.getVersionDownloadsCount(this._transformDaoParams(daoParams)).$promise;
	}

	getManifest(daoParams) {
		return this.packagesDAO.getManifest(this._transformDaoParams(daoParams)).$promise;
	}

	getVersion(daoParams) {
		return this.packagesDAO.getVersion(this._transformDaoParams(daoParams)).$promise;
	}

    _transformDaoParams(daoParams) {
        let fixed = {};

        let fix = (from, to) => {
            if (daoParams[from]) fixed[to] = daoParams[from];
        };

        fix('package', 'packageName');
        fix('version', 'versionName');
        fix('packageType', 'packageType');
        fix('repo', 'repo');
        fix('manifest', 'manifest');
        fix('order', 'order');
        fix('sort_by', 'sort_by');
        fix('$no_spinner', '$no_spinner');

        return fixed;

    }
	// TODO: When the product team will decide on phase 2 (adding xray) , replace the 'false' value
	isXrayEnabled() {
		return this.footerDao.get(false).then((footerData) => {
			return false;//footerData.xrayEnabled && footerData.xrayConfigured && footerData.xrayLicense;
		});
	}

	showInTree(pathParams) {
		let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
		if (browser === 'stash') {
			browser = 'tree';
		}
		let path = `${pathParams.repo}/${pathParams.package}/${pathParams.version}`;
		this.$state.go('artifacts.browsers.path', {
			tab: 'General',
			artifact: path,
			browser: browser
		});
	}
}
