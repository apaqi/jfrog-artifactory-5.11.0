import {BundlesListController} from "../bundles/bundles_list.controller";
import {BundlePageController} from "../bundles/bundle_page.controller";

function bundlesConfig ($stateProvider) {

    $stateProvider
            .state('bundles', {
                url: '/bundles',
                templateUrl: 'states/bundles/bundles.html',
                parent: 'app-layout',
            })
            .state('bundles.list', {
                url: '/',
                templateUrl: 'states/bundles/bundles_list.html',
                controller: 'BundlesListController as BundlesList'
            })
            .state('bundles.bundle_page', {
                url: '/{bundleName}/{version}',
                templateUrl: 'states/bundles/bundle_page.html',
                controller: 'BundlePageController as BundlePage'
            })
}

export default angular.module('bundles', [])
        .config(bundlesConfig)
        .controller('BundlesListController', BundlesListController)
        .controller('BundlePageController', BundlePageController)