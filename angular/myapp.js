(function () {
    'use strict';
 
    angular
        .module('myApp', ['ngRoute', 'ngCookies','ngTable','angular-md5','ngConfirm','ui.calendar','ui.bootstrap'])
        .config(config)
        .run(run);
 
    config.$inject = ['$routeProvider', '$locationProvider'];
    function config($routeProvider, $locationProvider) {
        $locationProvider.html5Mode(true).hashPrefix('!');
        $routeProvider
            .when('/', {
                templateUrl: 'index.html'
            })
            
            .when('/tracktime', {
                templateUrl: 'tracktime.html', controller: 'tracktimecontroller'
            })
        
            .when('/overview', {
                templateUrl: 'overview.html', controller: 'overviewcontroller'
            })
            
            .when('/reporting', {
                templateUrl: 'reporting.html'
            })
        
            .when('/login', {
                templateUrl: 'index.html'
            })
 
            .otherwise({ redirectTo: '/' });
    }
 
    run.$inject = ['$rootScope', '$location', '$cookieStore', '$http'];
    function run($rootScope, $location, $cookieStore, $http) {
        // keep user logged in after page refresh
        $rootScope.globals = $cookieStore.get('globals') || {};
        if ($rootScope.globals.currentUser) {
            $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata; // jshint ignore:line
        }
 
        $rootScope.$on('$locationChangeStart', function (event, next, current) {
            // redirect to login page if not logged in and trying to access a restricted page
            var restrictedPage = $.inArray($location.path(), ['/tracktime', '/overview', '/reporting']) === -1;
            var loggedIn = $rootScope.globals.currentUser;
            if (restrictedPage && !loggedIn) {
                $location.path('/');
            }
        });
    }
 
})();