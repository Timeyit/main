angular.module('myApp')
    .controller('SignupController', function($location, $log, $scope, $http, md5, AuthenticationService, $window) {

    var vm = this;
    $scope.vm = vm;
    $scope.error = "";

    $scope.signUp = function()
    {
        $log.log('Signup Controller - Validating Data');
        if(!angular.isDefined(vm.username) || vm.username == "")
        {
            $scope.error = "Username not defined.";
            return;
        }

        if(!angular.isDefined(vm.email) || vm.email == "")
        {
            $scope.error = "Email not defined.";
            return;
        }

        if(!angular.isDefined(vm.password) || vm.password == "")
        {
            $scope.error = "Password not defined.";
            return;
        }

        if(vm.password != vm.passwordRep)
        {
            $scope.error = "Passwords do not match.";
            return;
        }
        $log.log('Signup Controller - Signing Up');

        $http.post('PHP/user_create.php', {
            'username' : vm.username,
            'password' : md5.createHash("mysalt" + vm.password),
            'email' : vm.email}
                  ).then(function (data, status, headers, config) {
            // Do nothing. Only persist.
            $log.log("Got data: " + data.data);
            if(data.data == "OK")
            {
                // Send confirmation email.
                $log.log('Sending welcome email');
                $http.post('PHP/send_welcome_email.php', {'username' : vm.username, 'email' : vm.email}
                          ).then(function (data, status, headers, config) {
                    $log.log('Sent welcome email');
                });

                AuthenticationService.Login(vm.username, md5.createHash("mysalt" + vm.password), function (response) {
                    if (response.success) {
                        $log.log("AuthenticationService success");
                        AuthenticationService.SetCredentials(vm.username, response.sessionkey);
                        //$location.path('/tracktime.html');
                        $log.log('http://' + $location.host() + ':' + $location.port() + '/tracktime.html');
                        $window.location.href = 'http://' + $location.host() + ':' + $location.port() + '/tracktime.html';
                    } else {
                        $log.log("AuthenticationService failed");
                        FlashService.Error(response.message);
                        vm.dataLoading = false;
                    }
                });
            }
            else if(data == "exists")
            {
                $scope.error = "Username unavailable.";
                vm.username = "";
                vm.password = "";
                vm.passwordRep = "";
            }
            else
            {
                $scope.error = "Failed to create user.";
            }
        });
    };
})