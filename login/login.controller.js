angular.module('myApp')
    .controller('LoginController', function($location, $log, $scope, AuthenticationService, FlashService, $window, md5, $http) {

    var vm = this;
    $scope.vm = vm;

    $scope.login = function () {
        if(vm.password == "")
        {
            return;
        }
        $log.log("Entering login()");
        vm.dataLoading = true;
        $log.log("Calling AuthenticationService");
        $log.log("Username: " + vm.username);
        $log.log("Password: " + md5.createHash("mysalt" + vm.password));

        AuthenticationService.Login(vm.username, md5.createHash("mysalt" + vm.password), function (response) {
            if (response.success) {
                $log.log("AuthenticationService success");
                AuthenticationService.SetCredentials(vm.username, md5.createHash("mysalt" + vm.password), response.sessionkey);
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

    $scope.signIn = function(authResult) {
        $scope.$apply(function() {
            $scope.processAuth(authResult);
        });
    }

    $scope.processAuth = function(authResult) {
        $scope.immediateFailed = true;
        if ($scope.isSignedIn) {
            return 0;
        }
        if (authResult['access_token']) {
            $scope.immediateFailed = false;
            // Successfully authorized, create session
            $log.log(authResult);
            if (authResult && !authResult.error) {
                var data = {};
                gapi.client.load('oauth2', 'v2', function() {
                    gapi.client.oauth2.userinfo.get().execute(function(resp) {
                        // Shows user email
                        console.log(resp.email);
                        if(resp.email != "")
                        {
                            AuthenticationService.SetCredentials(resp.email, "gmail");
                            $http.post('PHP/user_create.php', {'username' : resp.email, 'password' : "", 'email' : resp.email}
                                      ).success(function (data, status, headers, config) {
                                $window.location.href = 'http://' + $location.host() + ':' + $location.port() + '/tracktime.html';
                            });
                            
                        }
                    })
                });

            } else {
                deferred.reject('error');
            }
        } else if (authResult['error']) {
            if (authResult['error'] == 'immediate_failed') {
                $scope.immediateFailed = true;
            } else {
                console.log('Error:' + authResult['error']);
            }
        }
    }

    /*$scope.renderSignIn = function() {
        gapi.signin.render('myGsignin', {
            'callback': $scope.signIn,
            'clientid': '86525225441-cfqovpohsnop75270shalha7rvihsdlv.apps.googleusercontent.com',
            'scope': 'https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email',
            'theme': 'dark',
            'cookiepolicy': 'single_host_origin',
            'accesstype': 'offline'
        });
    }

    $scope.renderSignIn();*/
})