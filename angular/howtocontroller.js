(function () {
    'use strict';
 
    angular
        .module('myApp')
        .controller('HowToController', HowToController);
 
    HowToController.$inject = ['AuthenticationService', '$location', '$rootScope', '$scope','$log'];
    function HowToController(AuthenticationService, $location, $rootScope, $scope, $log) {
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
        
        
        
    }
})();