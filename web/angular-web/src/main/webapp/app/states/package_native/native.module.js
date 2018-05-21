import PackagesNativeController from "./native.controller";

export const MODULE_PACKAGE_NATIVE = 'packages';

function NativeConfig($stateProvider) {
    $stateProvider
            .state({
                name: 'packagesNative',
                url: '/packages/{subRouterPath:JFrogSubRouterPath}',
                controller: 'PackagesNativeController',
                controllerAs: '$ctrl',
                template: require('./native.view.html'),
                parent: 'app-layout',
            });
}

angular.module(MODULE_PACKAGE_NATIVE, [])
        .config(NativeConfig)
        .controller('PackagesNativeController', PackagesNativeController);
