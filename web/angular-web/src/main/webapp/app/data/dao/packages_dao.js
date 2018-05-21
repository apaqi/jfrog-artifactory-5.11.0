export class PackagesDAO {
    constructor(RESOURCE, ArtifactoryDaoFactory) {
        let path  = `${RESOURCE.API_URL}/v1`;
        return ArtifactoryDaoFactory()
                .setPath(path)
                .setCustomActions({
                    'getPackages': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/packages/:packageType/list`,
                    },
                    'getFilters': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/repos/:packageType`,
                        params: {
                            packageType: '@packageType',
                        },
                    },
                    'getPackage': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/packages/:packageType`,
                        params: {
                            packageType: '@packageType',
                        }
                    },
                    'getPackageDownloadsCount': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/packages/total_downloads/:packageType`,
                        params: {
                            packageType: '@packageType'
                        }
                    },
                    'getPackageExtraInfo': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/packages/extra_info/:packageType`,
                        params: {
                            packageType: '@packageType'
                        }
                    },
                    'getVersionDownloadsCount': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/versions/total_downloads/:packageType/:repo`,
                        params: {
                            packageType: '@packageType',
	                        repo: '@repo'
                        }
                    },
                    'getVersion': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/versions/:packageType/:repo`,
                        params: {
                            packageType: '@packageType',
                            repo: '@repo'
                        },
                    },
                    'getManifest': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/versions/:packageType/:repo`,
                        params: {
                            packageType: '@packageType',
                            repoKey: '@repo'
                        },
                    }
                })
                .getInstance();
    }
}

