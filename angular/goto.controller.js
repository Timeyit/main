(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('GoToController', GoToController);

    GoToController.$inject = ['AuthenticationService', '$location','$rootScope', '$scope','$log','$http','$window'];
    function GoToController(AuthenticationService, $location, $rootScope, $scope, $log, $http, $window) {
        $log.log("GoTo Controller: Verifying session");
        var absUrl = $location.absUrl();
        $log.log("absUrl: " + absUrl);
        var sessionkey = $location.search()['sessionkey'];
        if(sessionkey == undefined)
        {
            var indexStart = absUrl.lastIndexOf("sessionkey=") + "sessionkey=".length;
            sessionkey = absUrl.substr(indexStart);
        }
        var page = $location.search()['page'];
        if(page == undefined)
        {
            var indexStart = absUrl.lastIndexOf("page=") + "page=".length;
            var indexEnd = absUrl.lastIndexOf("sessionkey=");
            page = absUrl.substr(indexStart, indexEnd-indexStart-1);
        }

        $log.log("GoTo Controller: sessionkey: " + sessionkey);
        $log.log("GoTo Controller: page: " + page);
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

                    AuthenticationService.SetCredentials(data.data, sessionkey);
                    $scope.NewURL = 'http://' + $location.host() + ':' + $location.port() + '/' + page;
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