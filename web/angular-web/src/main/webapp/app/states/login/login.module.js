import {LoginController} from './login.controller';

function loginConfig($stateProvider) {
    $stateProvider
        .state('login', {
            url: '/login?redirectTo',
            templateUrl: 'states/login/login.html',
            controller: 'LoginController as Login',
            params: {oauthError: null},
            parent: 'login-layout'
        })
        .state('logout', {
            url: '/logout',
            onEnter: (JFrogEventBus) => LoginController.staticLogout(JFrogEventBus, true),
            parent: 'login-layout'
        });
}

export default angular.module('changePassword', [])
    .config(loginConfig)
        .controller('LoginController', LoginController);