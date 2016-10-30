(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('GoToController', GoToController);

    GoToController.$inject = ['AuthenticationService', '$location', '$rootScope', '$scope','$log','$http','$window'];
    function GoToController(AuthenticationService, $location, $rootScope, $scope, $log, $http, $window) {

        $http.post('PHP/session_verifySession.php', {sessionkey : $location.search()['sessionkey']}
                  ).success(function (data, status, headers, config) {
            if(data == 'OK')
            {
                $http.post('PHP/session_getUsername.php', {sessionkey : $location.search()['sessionkey']}
                  ).success(function (data, status, headers, config) {
                    AuthenticationService.SetCredentials(data, "goto", $location.search()['sessionkey']);
                });
                $scope.NewURL = 'http://' + $location.host() + ':' + $location.port() + '/' + $location.search()['page'];
            }
            else
            {
                $scope.NewURL = 'http://' + $location.host() + ':' + $location.port() + '/' + 'index.html';
            }

            $window.location.href = $scope.NewURL;
        });
    }
})();