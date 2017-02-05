(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('GoToController', GoToController);

    GoToController.$inject = ['AuthenticationService', '$location', '$rootScope', '$scope','$log','$http','$window'];
    function GoToController(AuthenticationService, $location, $rootScope, $scope, $log, $http, $window) {
        $log.log("GoTo Controller: Verifying session");
        var sessionkey = $location.search()['sessionkey'];
        $log.log("GoTo Controller: sessionkey: " + sessionkey);
        $log.log("GoTo Controller: page: " + $location.search()['page']);
        $http.post('PHP/session_verifySession.php', {'sessionkey' : sessionkey}
                  ).then(function (data, status, headers, config) {
            if(data.data == 'OK')
            {
                $log.log("GoTo Controller: Session ok. Getting username.");
                //AuthenticationService.SetCredentials('', sessionkey);
                $http.post('PHP/session_getUsername.php', {'sessionkey' : sessionkey}
                  ).then(function (data, status, headers, config) {
                    $log.log("GoTo Controller: Got username - " + data.data);
                    $log.log("GoTo Controller: Username ok, updating credentials");
                    
                    AuthenticationService.SetCredentials(data.data, $location.search()['sessionkey']);
                    $scope.NewURL = 'http://' + $location.host() + ':' + $location.port() + '/' + $location.search()['page'];
                    $window.location.href = $scope.NewURL;
                });
                
            }
            else
            {
                $scope.NewURL = 'http://' + $location.host() + ':' + $location.port() + '/' + 'index.html';
                $window.location.href = $scope.NewURL;
            }
        });
    }
})();