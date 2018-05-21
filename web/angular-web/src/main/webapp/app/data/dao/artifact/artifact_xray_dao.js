import {ArtifactXrayDao} from '../../artifactory_dao';

export function ArtifactXrayDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'GET'})
            .setPath(RESOURCE.ARTIFACT_XRAY)
            .setCustomActions({
                'getData': {
                    params: {
                        repoKey: '@repoKey',
                        path: '@path'
                    },
                    notifications: true
                },
                'isBlocked': {
                    path: RESOURCE.ARTIFACT_XRAY + '/isBlocked',
                    params: {
                        repoKey: '@repoKey',
                        path: '@path'
                    },
                    notifications: true
                },
                'isArtifactBlockedAsync': {
                    path: RESOURCE.ARTIFACT_XRAY + '/isBlocked',
                    params: {
                        $no_spinner: true,
                        repoKey: '@repoKey',
                        path: '@path'
                    }
                }
            })
            .getInstance();
}
