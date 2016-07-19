(function () {
    'use strict';
 
    angular
        .module('myApp')
        .factory('UserService', UserService);
 
    UserService.$inject = ['$timeout', '$filter', '$q','$log'];
    function UserService($timeout, $filter, $q, $log) {
 
        var service = {};
 
        service.GetAll = GetAll;
        service.GetById = GetById;
        service.GetByUsername = GetByUsername;
        service.Create = Create;
        service.Update = Update;
        service.Delete = Delete;
 
        return service;
 
        function GetAll() {
            $log.log('Get all called');
            var deferred = $q.defer();
            deferred.resolve(getUsers());
            $log.log(deferred.promise);
            return deferred.promise;
        }
 
        function GetById(id) {
            var deferred = $q.defer();
            var filtered = $filter('filter')(getUsers(), { id: id });
            var user = filtered.length ? filtered[0] : null;
            deferred.resolve(user);
            return deferred.promise;
        }
 
        function GetByUsername(username) {
            $log.log("User Service Local Storage - GetByUsername()");
            $log.log("Username: " + username);
            var deferred = $q.defer();
            $log.log("GetByUsername() -> GetUsers()");
            var allusers = getUsers();
            $log.log("all users: " + allusers)
            var filtered = $filter('filter')(getUsers(), { username: username });
            $log.log("GetUsers() -> GetByUsername()");
            $log.log("Filtered results: " + filtered);
            var user = filtered.length ? filtered[0] : null;
            $log.log("user object: " + user);
            $log.log("user object: " + user.username);
            deferred.resolve(user);
            $log.log("deferred: " + deferred)
            return deferred.promise;
        }
 
        function Create(user) {
            var deferred = $q.defer();
 
            // simulate api call with $timeout
            $timeout(function () {
                GetByUsername(user.username)
                    .then(function (duplicateUser) {
                        if (duplicateUser !== null) {
                            deferred.resolve({ success: false, message: 'Username "' + user.username + '" is already taken' });
                        } else {
                            var users = getUsers();
 
                            // assign id
                            var lastUser = users[users.length - 1] || { id: 0 };
                            user.id = lastUser.id + 1;
 
                            // save to local storage
                            users.push(user);
                            setUsers(users);
 
                            deferred.resolve({ success: true });
                        }
                    });
            }, 1000);
 
            return deferred.promise;
        }
 
        function Update(user) {
            var deferred = $q.defer();
 
            var users = getUsers();
            for (var i = 0; i < users.length; i++) {
                if (users[i].id === user.id) {
                    users[i] = user;
                    break;
                }
            }
            setUsers(users);
            deferred.resolve();
 
            return deferred.promise;
        }
 
        function Delete(id) {
            var deferred = $q.defer();
 
            var users = getUsers();
            for (var i = 0; i < users.length; i++) {
                var user = users[i];
                if (user.id === id) {
                    users.splice(i, 1);
                    break;
                }
            }
            setUsers(users);
            deferred.resolve();
 
            return deferred.promise;
        }
 
        // private functions
 
        function getUsers() {
            $log.log("GetUsers called");
            $log.log("Defining localStorage.users");
            localStorage.users = JSON.stringify(
                [ 
                    {id : 1, username : 'simon', password : 'mypassword'}, 
                    {id : 2, username : 'rati', password : 'mypassword'}, 
                    {id : 3, username : 'admin', password : 'mypassword'}
                ]);
            //}
            $log.log(localStorage.users);
            return JSON.parse(localStorage.users);
        }
 
        function setUsers(users) {
            localStorage.users = JSON.stringify(users);
        }
    }
})();