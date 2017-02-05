angular.module('myApp')

    .controller('tracktimecontroller', function($scope, $http, $location, $filter, ngTableParams, $window, $interval, UserService, AuthenticationService,$log) {

    $scope.myData = [];
    $scope.idTimeLog = -1;
    $scope.lapTime = 0;

    var vm = this;

    $scope.user = AuthenticationService.GetUsername();
    $scope.sessionkey = AuthenticationService.GetSessionKey();
    
    $scope.allUsers = [];/*
    vm.deleteUser = deleteUser;*/
    updateInterval = 2;

    initController();

    $scope.host = $location.host();
    $scope.port = $location.port();

    $scope.previousTask = "";
    $scope.currentTask = "No task selected";
    $scope.totalTime = "";
    $scope.currentIndex = -1;
    $scope.isTracking = false;

    var stop;

    $scope.getAll = function()
    {
        $log.log("Getting work items");
        $log.log("$scope.sessionkey: " + $scope.sessionkey);
        $http.post('PHP/workItem_getAll.php', {
            'sessionkey' : $scope.sessionkey
        }
                  ).then(function (data, status, headers, config) {
            $log.log("Got data: " + data.data);
            $log.log(data.data);
            $scope.myData = [];
            angular.forEach(data.data, function(value, key) {
                if (angular.isUndefined(value.nameWorkItem) || value.nameWorkItem == null)
                {
                    // do nothing
                }
                else
                {
                    $scope.myData.push({task : value.nameWorkItem, totalTime : value.duration, idworkItem: value.idworkItem});
                }
            });
        });
        // call get tasks here
    }
    $scope.getAll(); 

    $scope.tableParams = new ngTableParams({
        sorting: {
            totalTime: 'asc'
        }},
                                           {
        getData: function($defer, params) {
            $scope.myData = $filter('orderBy')($scope.myData, params.orderBy());
            $defer.resolve($scope.myData);
        }
    });

    $scope.select = function(x) {
        $scope.stopTimer();
        $scope.lapTime = 0;
        $scope.idTimeLog = -1;
        $scope.previousTask = x.task;
        $scope.currentIndex = $scope.myData.indexOf(x);
        $scope.currentTask = $scope.myData[$scope.currentIndex].task;
        $scope.totalTime = $scope.myData[$scope.currentIndex].totalTime;
    };

    $scope.selectAndTrack = function(x) {
        if(x.task != $scope.previousTask || !$scope.isTracking)
        {
            $scope.lapTime = 0;
            $scope.idTimeLog = -1;
            $scope.previousTask = x.task;
            $scope.currentIndex = $scope.myData.indexOf(x);
            $scope.currentTask = $scope.myData[$scope.currentIndex].task;
            $scope.totalTime = $scope.myData[$scope.currentIndex].totalTime;
            // Launch timer
            $scope.startTimer();
        }
        else
        {
            $scope.previousTask = "";
            $scope.currentTask = "No task selected";
            $scope.totalTime = "";
            $scope.currentIndex = -1;
        }
    };

    $scope.deleteWorkItem = function(x) {
        $scope.currentIndex = $scope.myData.indexOf(x);
        $http.post('PHP/workItem_delete.php', {
            'idworkItem' : $scope.myData[$scope.currentIndex].idworkItem,
            'sessionkey' : $scope.sessionkey
        }
                  ).success(function (data, status, headers, config) {
            // refresh the list
            $scope.getAll();
        });
    };

    $scope.startTimer = function() {
        if ( angular.isDefined(stop) ) return;
        $scope.isTracking = true;
        stop = $interval(function() {
            if ($scope.isTracking)
            {
                if ($scope.myData[$scope.currentIndex].task != "") {
                    $scope.myData[$scope.currentIndex].totalTime = parseInt($scope.myData[$scope.currentIndex].totalTime,10) + 1;
                    $scope.lapTime = parseInt($scope.lapTime, 10) + 1;
                    $scope.totalTime = $scope.myData[$scope.currentIndex].totalTime;
                    if($scope.lapTime % updateInterval == 0)
                    {
                        $http.post('PHP/workItem_update.php', {
                            'duration' : $scope.totalTime,
                            'idworkItem' : $scope.myData[$scope.currentIndex].idworkItem,
                            'sessionkey' : $scope.sessionkey
                        }
                                  ).success(function (data, status, headers, config) {
                            // Do nothing. Only persist.
                        });

                        $http.post('PHP/timeLog_updateCreate.php', {'durationLap' : $scope.lapTime, 
                                                                    'idworkItem' : $scope.myData[$scope.currentIndex].idworkItem, 
                                                                    'idTimeLog' : $scope.idTimeLog,
                                                                    'sessionkey' : $scope.sessionkey
                                                                   }
                                  ).success(function (data, status, headers, config) {
                            // Do nothing. Only persist.

                            newTimeId = data[0].myid;
                            if(newTimeId >= 0)
                            {
                                $scope.idTimeLog = newTimeId;
                            }

                        });
                    }

                } else {
                    $scope.stopTimer();
                }
            }

        }, 1000);
    };

    $scope.stopTimer = function() {
        $scope.isTracking = false;
        if (angular.isDefined(stop)) {
            $interval.cancel(stop);
            stop = undefined;
        }
    };

    // Local functions
    function initController() {
        //loadCurrentUser();
        loadAllUsers();
    }

    function loadAllUsers() {
        UserService.GetAll()
            .then(function (users) {
            vm.allUsers = users;
        });
    }

    function deleteUser(id) {
        UserService.Delete(id)
            .then(function () {
            loadAllUsers();
        });
    }

    // Desctructor
    $scope.$on('$destroy', function() {
        // Make sure that the interval is destroyed too
        $scope.stopTimer();
    });

    // create new Work Item 
    $scope.createWorkItem = function(){
        //$log.log("Function: createWorkItem()");
        // fields in key-value pairs
        if($scope.nameWorkItem != '')
        {
            $http.post('PHP/workItem_create.php', {
                'itemName' : $scope.nameWorkItem, 
                'sessionkey' : $scope.sessionkey
            }
                      ).success(function (data, status, headers, config) {

                $scope.nameWorkItem = '';

                // refresh the list
                $scope.getAll();
            });
        }

    }

    window.onbeforeunload = function (event) {
        if($scope.isTracking == true)
        {
            var message = 'You are currently tracking time. Are you sure you want to leave this page?';
            if (typeof event == 'undefined') {
                event = window.event;
            }
            if (event) {
                event.returnValue = message;
            }
            return message;
        }

    }
})


// Register the 'myCurrentTime' directive factory method.
// We inject $interval and dateFilter service since the factory method is DI.
    .directive('myCurrentTime', ['$interval', 'dateFilter',
                                 function($interval, dateFilter) {
                                     // return the directive link function. (compile function not needed)
                                     return function(scope, element, attrs) {
                                         var format,  // date format
                                             stopTime; // so that we can cancel the time updates

                                         // used to update the UI
                                         function updateTime() {
                                             element.text(dateFilter(new Date(), format));
                                         }

                                         // watch the expression, and update the UI on change.
                                         scope.$watch(attrs.myCurrentTime, function(value) {
                                             format = value;
                                             updateTime();
                                         });

                                         stopTime = $interval(updateTime, 1000);

                                         // listen on DOM destroy (removal) event, and cancel the next UI update
                                         // to prevent updating time after the DOM element was removed.
                                         element.on('$destroy', function() {
                                             $interval.cancel(stopTime);
                                         });
                                     }
                                 }])

    .directive( "mwConfirmClick", [
    function( ) {
        return {
            priority: -1,
            restrict: 'A',
            scope: { confirmFunction: "&mwConfirmClick" },
            link: function( scope, element, attrs ){
                element.bind( 'click', function( e ){
                    // message defaults to "Are you sure?"
                    var message = attrs.mwConfirmClickMessage ? attrs.mwConfirmClickMessage : "Are you sure?";
                    // confirm() requires jQuery
                    if( confirm( message ) ) {
                        scope.confirmFunction();
                    }
                });
            }
        }
    }])

    .filter('secondsToDateTime', [function() {
        return function(seconds) {
            return new Date(1970, 0, 1).setSeconds(seconds);
        };
    }])

;


    