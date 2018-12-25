/*******************************************************************************
 * Metronic AngularJS App Main Script
 ******************************************************************************/

/* Metronic App */
var MetronicApp = angular.module("MetronicApp", [ "ui.router", "ui.bootstrap",
    "oc.lazyLoad", "ngSanitize" ]);

/* Configure ocLazyLoader(refer: https://github.com/ocombe/ocLazyLoad) */
MetronicApp.config([ '$ocLazyLoadProvider', function($ocLazyLoadProvider) {
    $ocLazyLoadProvider.config({
        // global configs go here
    });
} ]);

/*******************************************************************************
 * BEGIN: BREAKING CHANGE in AngularJS v1.3.x:
 ******************************************************************************/
/**
 * `$controller` will no longer look for controllers on `window`. The old
 * behavior of looking on `window` for controllers was originally intended for
 * use in examples, demos, and toy apps. We found that allowing global
 * controller functions encouraged poor practices, so we resolved to disable
 * this behavior by default.
 *
 * To migrate, register your controllers with modules rather than exposing them
 * as globals:
 *
 * Before:
 *
 * ```javascript function MyController() { // ... } ```
 *
 * After:
 *
 * ```javascript angular.module('myApp', []).controller('MyController',
 * [function() { // ... }]);
 *
 * Although it's not recommended, you can re-enable the old behavior like this:
 *
 * ```javascript angular.module('myModule').config(['$controllerProvider',
 * function($controllerProvider) { // this option might be handy for migrating
 * old apps, but please don't use it // in new ones!
 * $controllerProvider.allowGlobals(); }]);
 */

// AngularJS v1.3.x workaround for old style controller declarition in HTML
MetronicApp.config([ '$controllerProvider', function($controllerProvider) {
    // this option might be handy for migrating old apps, but please don't use
    // it
    // in new ones!
    $controllerProvider.allowGlobals();
} ]);

/*******************************************************************************
 * END: BREAKING CHANGE in AngularJS v1.3.x:
 ******************************************************************************/

/* Setup global settings */
MetronicApp.factory('settings', [ '$rootScope', function($rootScope) {
    // supported languages
    var settings = {
        layout : {
            pageSidebarClosed : false, // sidebar menu state
            pageBodySolid : false, // solid body color state
            pageAutoScrollOnLoad : 1000
            // auto scroll to top on page load
        },
        layoutImgPath : Metronic.getAssetsPath() + 'admin/layout/img/',
        layoutCssPath : Metronic.getAssetsPath() + 'admin/layout/css/'
    };

    $rootScope.settings = settings;

    return settings;
} ]);

/* Setup App Main Controller */
MetronicApp.controller('AppController', [ '$scope', '$rootScope',
    function($scope, $rootScope) {
        $scope.$on('$viewContentLoaded', function() {
            Metronic.initComponents(); // init core components
            // Layout.init(); // Init entire layout(header, footer, sidebar,
            // etc) on page load if the partials included in server side
            // instead of loading with ng-include directive
        });
    } ]);

/*******************************************************************************
 * Layout Partials. By default the partials are loaded through AngularJS
 * ng-include directive. In case they loaded in server side(e.g: PHP include
 * function) then below partial initialization can be disabled and Layout.init()
 * should be called on page load complete as explained above.
 ******************************************************************************/

/* Setup Layout Part - Header */
MetronicApp.controller('HeaderController', [ '$scope', function($scope) {
    $scope.$on('$includeContentLoaded', function() {
        Layout.initHeader(); // init header
    });
} ]);

/* Setup Layout Part - Sidebar */
MetronicApp.controller('SidebarController', [ '$scope', function($scope) {
    $scope.$on('$includeContentLoaded', function() {
        Layout.initSidebar(); // init sidebar
    });
} ]);

/* Setup Layout Part - Quick Sidebar */
MetronicApp.controller('QuickSidebarController', [ '$scope', function($scope) {
    $scope.$on('$includeContentLoaded', function() {
        setTimeout(function() {
            QuickSidebar.init(); // init quick sidebar
        }, 2000)
    });
} ]);

/* Setup Layout Part - Theme Panel */
MetronicApp.controller('ThemePanelController', [ '$scope', function($scope) {
    $scope.$on('$includeContentLoaded', function() {
        Demo.init(); // init theme panel
    });
} ]);

/* Setup Layout Part - Footer */
MetronicApp.controller('FooterController', [ '$scope', function($scope) {
    $scope.$on('$includeContentLoaded', function() {
        Layout.initFooter(); // init footer
    });
} ]);

/* Setup Rounting For All Pages */
MetronicApp
    .config([
        '$stateProvider',
        '$urlRouterProvider',
        function($stateProvider, $urlRouterProvider) {
            // Redirect any unmatched url
            $urlRouterProvider.otherwise("/dashboard.html");

            $stateProvider
            // Dashboard
                .state(
                    'dashboard',
                    {
                        url : "/dashboard.html",
                        templateUrl : "/loan-manage/dashboard.html",
                        data : {
                            pageTitle : 'Admin Dashboard Template'
                        },
                        controller : "DashboardController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/css/style.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #za
                .state(
                    'checkbyone',
                    {
                        url : "/checkbyone.html",
                        templateUrl : "/loan-manage/page_check/checkbyone.html",
                        data : {
                            pageTitle : 'checkbyone'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/md5.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_check/checkbyone.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #zd
                .state(
                    'checkbypass',
                    {
                        url : "/checkbypass.html",
                        templateUrl : "/loan-manage/page_check/checkbypass.html",
                        data : {
                            pageTitle : 'checkbypass'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/md5.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_check/checkbypass.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #zma
                .state(
                    'checkbymanualone',
                    {
                        url : "/checkbymanualone.html",
                        templateUrl : "/loan-manage/page_check/checkbymanualone.html",
                        data : {
                            pageTitle : 'checkbymanualone'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/md5.js',
                                                '/loan-manage/js_check/checkbymanualone.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #zmr
                .state(
                    'checkbymanual_result',
                    {
                        url : "/checkbymanual_result.html",
                        templateUrl : "/loan-manage/page_check/checkbymanual_result.html",
                        data : {
                            pageTitle : 'checkbymanual_result'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/md5.js',
                                                '/loan-manage/js_check/checkbymanual_result.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #zb
                .state(
                    'checkbymanager',
                    {
                        url : "/checkbymanager.html",
                        templateUrl : "/loan-manage/page_check/checkbymanager.html",
                        data : {
                            pageTitle : 'checkbymanager'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/md5.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_check/checkbymanager.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #zc
                .state(
                    'managerbymanager',
                    {
                        url : "/managerbymanager.html",
                        templateUrl : "/loan-manage/page_check/managerbymanager.html",
                        data : {
                            pageTitle : 'managerbymanager'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_check/managerbymanager.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'manualAuditReport',
                    {
                        url : "/manualAuditReport.html",
                        templateUrl : "/loan-manage/page_check/manualAuditReport.html",
                        data : {
                            pageTitle : 'manualAuditReport'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_check/manualAuditReport.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #zd
                .state(
                    'checkbyoned',
                    {
                        url : "/checkbyoned.html",
                        templateUrl : "/loan-manage/page_check/checkbyoned.html",
                        data : {
                            pageTitle : 'checkbyoned'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/md5.js',
                                                '/loan-manage/js_check/checkbyoned.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #zf
                .state(
                    'auditLoan',
                    {
                        url : "/auditLoan.html",
                        templateUrl : "/loan-manage/page_check/auditLoan.html",
                        data : {
                            pageTitle : 'auditLoan'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_check/auditLoan.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #ua
                .state(
                    'perlist',
                    {
                        url : "/perlist.html",
                        templateUrl : "/loan-manage/page_per/perlist.html",
                        data : {
                            pageTitle : 'perlist'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_per/perlist.js',
                                                '/loan-manage/js/tableUtils.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #ua
                .state(
                    'channelPerlist',
                    {
                        url : "/channelPerlist.html",
                        templateUrl : "/loan-manage/page_per/channelPerlist.html",
                        data : {
                            pageTitle : 'channelPerlist'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_per/channelPerlist.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #ya
                .state(
                    'feedback',
                    {
                        url : "/feedback.html",
                        templateUrl : "/loan-manage/page_youmi/feedback.html",
                        data : {
                            pageTitle : 'feedback'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_youmi/feedback.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #yb
                .state(
                    'question',
                    {
                        url : "/question.html",
                        templateUrl : "/loan-manage/page_youmi/question.html",
                        data : {
                            pageTitle : 'question'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_youmi/question.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #yc
                .state(
                    'message_temp',
                    {
                        url : "/message_temp.html",
                        templateUrl : "/loan-manage/page_youmi/message_temp.html",
                        data : {
                            pageTitle : 'message_temp'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_youmi/message_temp.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #va
                .state(
                    'coupon',
                    {
                        url : "/coupon.html",
                        templateUrl : "/loan-manage/page_operation/coupon.html",
                        data : {
                            pageTitle : 'coupon'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_operation/coupon.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                // #ga
                .state(
                    'lookup_type_manage',
                    {
                        url : "/lookup_type_manage.html",
                        templateUrl : "/loan-manage/page_fnd/lookup_type_manage.html",
                        data : {
                            pageTitle : 'lookup_type_manage'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                //#rm
                .state(
                    'repayment',
                    {
                        url : "/repayment.html",
                        templateUrl : "/loan-manage/page_loan/repayment.html",
                        data : {
                            pageTitle : 'repayment'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/md5.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_loan/repayment.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'repaymentPlan',
                    {
                        url : "/repaymentPlan.html",
                        templateUrl : "/loan-manage/page_loan/repaymentPlan.html",
                        data : {
                            pageTitle : 'repaymentPlan'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_loan/repaymentPlan.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'paymentInfo',
                    {
                        url : "/paymentInfo.html",
                        templateUrl : "/loan-manage/page_loan/paymentInfo.html",
                        data : {
                            pageTitle : 'paymentInfo'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js_loan/paymentInfo.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'loanManagement',
                    {
                        url : "/loanManagement.html",
                        templateUrl : "/loan-manage/page_loan/loanManagement.html",
                        data : {
                            pageTitle : 'loanManagement'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js_loan/loanManagement.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'batchReduce',
                    {
                        url : "/batchReduce.html",
                        templateUrl : "/loan-manage/page_loan/batchReduce.html",
                        data : {
                            pageTitle : 'batchReduce'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js_loan/batchReduce.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'userTables',
                    {
                        url : "/userTables.html",
                        templateUrl : "/loan-manage/page_auth/userTables.html",
                        data : {
                            pageTitle : 'userTables'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js_auth/userTables.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'modifyPassword',
                    {
                        url : "/modifyPassword.html",
                        templateUrl : "/loan-manage/page_auth/modifyPassword.html",
                        data : {
                            pageTitle : 'modifyPassword'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js_auth/modifyPassword.js',
                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'companyTables',
                    {
                        url : "/companyTables.html",
                        templateUrl : "/loan-manage/page_auth/companyTables.html",
                        data : {
                            pageTitle : 'companyTables'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js_auth/companyTables.js',
                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'autoLoan',
                    {
                        url : "/autoLoan.html",
                        templateUrl : "/loan-manage/page_auth/autoLoan.html",
                        data : {
                            pageTitle : 'autoLoan'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_auth/autoLoan.js',
                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'collectionList',
                    {
                        url : "/collectionList.html",
                        templateUrl : "/loan-manage/page_loan/collectionList.html",
                        data : {
                            pageTitle : 'collectionList'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_loan/collectionList.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'collectionInfo',
                    {
                        url : "/collectionInfo.html",
                        templateUrl : "/loan-manage/page_loan/collectionInfo.html",
                        data : {
                            pageTitle : 'collectionInfo'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_loan/collectionInfo.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'beatchConllectionList',
                    {
                        url : "/beatchConllectionList.html",
                        templateUrl : "/loan-manage/page_loan/beatchConllectionList.html",
                        data : {
                            pageTitle : 'beatchConllectionList'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/md5.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_loan/beatchConllectionList.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'repaymentReportForms',
                    {
                        url : "/repaymentReportForms.html",
                        templateUrl : "/loan-manage/page_loan/repaymentReportForms.html",
                        data : {
                            pageTitle : 'repaymentReportForms'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_loan/repaymentReportForms.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'inviteFriends',
                    {
                        url : "/inviteFriends.html",
                        templateUrl : "/loan-manage/page_commission/inviteFriends.html",
                        data : {
                            pageTitle : 'repaymentReportForms'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_commission/inviteFriends.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'uploadElectronicCard',
                    {
                        url : "/uploadElectronicCardTables.html",
                        templateUrl : "/loan-manage/page_commission/uploadElectronicCardTables.html",
                        data : {
                            pageTitle : 'repaymentReportForms'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_commission/uploadElectronicCardTables.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'commissionRule',
                    {
                        url : "/commissionRuleTables.html",
                        templateUrl : "/loan-manage/page_commission/commissionRuleTables.html",
                        data : {
                            pageTitle : 'repaymentReportForms'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_commission/commissionRuleTables.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'commission',
                    {
                        url : "/commissionReviewTables.html",
                        templateUrl : "/loan-manage/page_commission/commissionReviewTables.html",
                        data : {
                            pageTitle : 'commissionReviewTables'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_commission/commissionReviewTables.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'commissionReviewBefore',
                    {
                        url : "/commissionReviewBeforeTables.html",
                        templateUrl : "/loan-manage/page_commission/commissionReviewBeforeTables.html",
                        data : {
                            pageTitle : 'repaymentReportForms'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_commission/commissionReviewBeforeTables.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'refundReview',
                    {
                        url : "/refundReviewTables.html",
                        templateUrl : "/loan-manage/page_refund/refundReviewTables.html",
                        data : {
                            pageTitle : 'refundReportForms'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/lookup_type_manage.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_refund/refundReviewTables.js',
                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'refundRecord',
                    {
                        url : "/refundRecord.html",
                        templateUrl : "/loan-manage/page_refund/refundRecord.html",
                        data : {
                            pageTitle : 'refundRecord'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/md5.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_refund/refundRecord.js',
                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'drainage',
                    {
                        url : "/drainage.html",
                        templateUrl : "/loan-manage/page_operation/drainage.html",
                        data : {
                            pageTitle : 'drainage'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_operation/drainage.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'sms_temp',
                    {
                        url : "/sms_temp.html",
                        templateUrl : "/loan-manage/page_youmi/sms_temp.html",
                        data : {
                            pageTitle : 'sms_temp'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_youmi/sms_temp.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'region_source',
                    {
                        url : "/register_source_manage.html",
                        templateUrl : "/loan-manage/page_fnd/register_source_manage.html",
                        data : {
                            pageTitle : 'region_source'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                'http://js.youmishanjie.com/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js_fnd/register_source_manage.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
                .state(
                    'product_manager',
                    {
                        url : "/product_manager.html",
                        templateUrl : "/loan-manage/page_fnd/product_manager.html",
                        data : {
                            pageTitle : 'product_manager'
                        },
                        controller : "GeneralPageController",
                        resolve : {
                            deps : [
                                '$ocLazyLoad',
                                function($ocLazyLoad) {
                                    return $ocLazyLoad
                                        .load({
                                            name : 'MetronicApp',
                                            insertBefore : '#ng_load_plugins_before',
                                            files : [
                                                '/loan-manage/js/controllers/GeneralPageController.js',
                                                '/loan-manage/devextreme/css/dx.common.css',
                                                '/loan-manage/devextreme/css/dx.light.css',
                                                '/loan-manage/devextreme/js/globalize.min.js',
                                                '/loan-manage/devextreme/js/dx.webappjs.js',
                                                '/loan-manage/devextreme/js/jszip.min.js',
                                                '/loan-manage/js/tableUtils.js',
                                                '/loan-manage/js_fnd/product_manager.js',

                                                '/loan-manage/js/controllers/GeneralPageController.js' ]
                                        });
                                } ]
                        }
                    })
            // end

        } ]);

/* Init global settings and run the app */
MetronicApp.run([ "$rootScope", "settings", "$state",
    function($rootScope, settings, $state) {
        $rootScope.$state = $state; // state to be accessed from view
    } ]);