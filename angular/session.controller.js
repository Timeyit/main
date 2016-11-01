(function () {
    'use strict';
 
    angular
        .module('myApp')
        .controller('SessionController', SessionController);
 
    SessionController.$inject = ['AuthenticationService', '$location', '$rootScope', '$scope','$window','$log'];
    function SessionController(AuthenticationService, $location, $rootScope, $scope, $window, $log) {
        var vm = this;
        
        $log.log("SessionController - Validating Credentials");
        if(angular.isDefined($rootScope.globals['currentUser']) && angular.isDefined($rootScope.globals['currentUser'].username))
        {
            $scope.myUser = $rootScope.globals['currentUser'].username;
        }
        else
        {
            $scope.myUser = "";
        }
        
        
        // Are we currently in index.html?
        if($location.absUrl().indexOf("index.html") > -1)
        {
            // Do nothing
        }
        else
        {
            // If we are some other place. Verify that we are logged in.
            if($scope.myUser == "")
            {
                $log.log("SessionController - Validation Failed");
                $window.location.href = 'http://' + $location.host() + ':' + $location.port() + '/index.html';
            }
        }
        
        $scope.checkLogin = function()
        {
            $log.log("SessionController - CheckLogin called");
            if($scope.myUser == "")
            {
                $log.log("SessionController - Redirecting to main");
                $window.location.href = 'http://' + $location.host() + ':' + $location.port() + '/index.html';
            }
        }
        
        $scope.logOff = function() {
            $log.log("SessionController - LogOff");
            //gapi.auth.signOut();
            AuthenticationService.SetCredentials("","");
            $window.location.href = 'http://' + $location.host() + ':' + $location.port() + '/index.html';
        };
        
    }
})();