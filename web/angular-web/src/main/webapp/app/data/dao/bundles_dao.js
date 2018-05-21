export function BundlesDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.BUNDLES + "/:name/:version")
        .setCustomActions({
            getData: {
                method: 'GET',
                isArray: false,
                params: {name : '@name'}
            },
            getBundleVersions: {
                method: 'GET',
                isArray: false,
                params: {name: '@name'}
            },
            getBundleData: {
                method: 'GET',
                isArray: false,
                params: {name: '@name', version: '@version'}
            }
        })
        .getInstance();
}
