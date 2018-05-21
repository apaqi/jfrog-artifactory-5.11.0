import {TrustedKeysController} from './trusted_keys.controller';

function trustedKeysConfig($stateProvider) {

	$stateProvider
		.state('admin.security.trusted_keys', {
			params: {feature: 'trustedkeys'},
			url: '/trusted_keys',
			templateUrl: 'states/admin/security/trusted_keys/trusted_keys.html',
			controller: 'TrustedKeysController as TrustedKeys'
		})
}

export default angular.module('security.trusted_keys', [])
                      .config(trustedKeysConfig)
                      .controller('TrustedKeysController', TrustedKeysController);