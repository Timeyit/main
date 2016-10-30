(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('GoToController', GoToController);

    GoToController.$inject = ['AuthenticationService', '$location', '$rootScope', '$scope','$log','$http','$window'];
    function GoToController(AuthenticationService, $location, $rootScope, $scope, $log, $http, $window) {
        $log.log("GoTo Controller: Verifying session");
        $log.log("GoTo Controller: sessionkey: " + $location.search()['sessionkey']);
        $log.log("GoTo Controller: page: " + $location.search()['page']);
        $http.post('PHP/session_verifySession.php', {sessionkey : $location.search()['sessionkey']}
                  ).success(function (data, status, headers, config) {
            if(data == 'OK')
            {
                $log.log("GoTo Controller: Session ok. Getting username.");
                $http.post('PHP/session_getUsername.php', {sessionkey : $location.search()['sessionkey']}
                  ).success(function (data, status, headers, config) {
                    $log.log("GoTo Controller: Got username - " + data);
                    $log.log("GoTo Controller: Username ok, updating credentials");
                    
                    AuthenticationService.SetCredentials(data, $location.search()['sessionkey']);
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