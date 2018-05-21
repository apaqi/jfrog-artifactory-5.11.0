import {ArtifactoryDao} from '../artifactory_dao';

export function TrustedKeysDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'GET'})
        .setPath(RESOURCE.TRUSTEDKEYS)
        .setCustomActions({
            'getTrustedKeys': {
                method: 'GET',
	            isArray: true
            },
            'AddTrustedKey': {
                method: 'POST',
                params: {'public_key': '@key', 'alias': '@alias'},
	            notifications: true
            },
            'deleteTrustedKey': {
            	path: RESOURCE.TRUSTEDKEYS + '/delete',
	            method: 'POST',
	            params: {'key_id': '@key_id'},
	            notifications: true
            }
        })
        .getInstance();
}