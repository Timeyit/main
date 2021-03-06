angular.module('myApp')

    .controller('reportingcontroller', function($scope, $http, $location, $filter, ngTableParams, $window, $interval, UserService, AuthenticationService,$log,$q) {

    $scope.myData = [];
    $scope.myDataAllDays = [];
    $scope.myDataAllWeeks = [];
    $scope.myDataAllMonths = [];
    $scope.myDataAllItems = [];
    $scope.myDataDaily = [];
    $scope.myDataWeekly = [];
    $scope.myDataMonthly = [];
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
    $scope.currentTask = "None Selected";
    $scope.totalTime = "";
    $scope.currentIndex = -1;
    $scope.isTracking = true;

    var stop;

    $log.log("Getting all time logs");
    return $q(function(resolve, reject) {
        $http.post('PHP/timeLog_getTimeLog.php', {'sessionkey' : AuthenticationService.GetSessionKey()}
                  ).then(function (data, status, headers, config) {
            $scope.myData = [];
            $scope.myDataAllDays = [];
            $scope.myDataAllWeeks = [];
            $scope.myDataAllMonths = [];
            $scope.myDataAllItems = [];
            $scope.myDataDaily = [];
            var newEvents = [];
            //$scope.myDataWeekly = [];
            //$scope.myDataMonthly = [];

            // Get all lap data
            var loop1 = 0;

            angular.forEach(data.data, function(value, key) {
                loop1 = loop1 + 1;
                if (angular.isUndefined(value.nameWorkItem) || value.nameWorkItem == null)
                {
                    // do nothing
                }
                else
                {

                    value.dateStartAsString = $filter('date')(value.TimeStart, "yyyy-MM-dd").substr(0,10);
                    var mdate = Date.parse(value.TimeStart);
                    var sdate = mdate/1000;
                    value.weekAsString = $filter('date')(mdate, "yyyy-ww");
                    value.monthAsString = $filter('date')(mdate, "yyyy-MMMM");
                    $scope.myData.push(
                        {
                            task : value.nameWorkItem, 
                            totalTime : value.Duration, 
                            idworkItem: value.idworkItem, 
                            timeStart : value.TimeStart, 
                            dateStart : value.dateStartAsString,
                            weekStart : value.weekAsString,
                            monthStart : value.monthAsString
                        });

                    // Make list of unique days
                    if ($scope.myDataAllDays.indexOf(value.dateStartAsString) == -1) {
                        $scope.myDataAllDays.push(value.dateStartAsString);
                    }

                    // Make list of unique weeks
                    if ($scope.myDataAllWeeks.indexOf(value.weekAsString) == -1) {
                        $scope.myDataAllWeeks.push(value.weekAsString);
                    }

                    // Make list of unique months
                    if ($scope.myDataAllMonths.indexOf(value.monthAsString) == -1) {
                        $scope.myDataAllMonths.push(value.monthAsString);
                    }

                    // Make list of unique work items
                    if ($scope.myDataAllItems.indexOf(value.nameWorkItem) == -1) {
                        $scope.myDataAllItems.push(value.nameWorkItem);
                    }
                }
            });

            // Categorize data per day, week and month (per item)
            angular.forEach($scope.myDataAllItems, function(valueItem, keyItem) {

                // Sum up all days
                angular.forEach($scope.myDataAllDays, function(valueDay, keyDay) {

                    // Initialize with 0
                    $scope.myDataDaily.push({task : valueItem, totalTime : 0, dateStart : valueDay});

                    angular.forEach($scope.myData, function(valueAll, keyAll) {
                        if(valueAll.task == valueItem && valueAll.dateStart == valueDay)
                        {
                            $scope.myDataDaily[$scope.myDataDaily.length - 1].totalTime = parseInt($scope.myDataDaily[$scope.myDataDaily.length - 1].totalTime,10) + parseInt(valueAll.totalTime,10);
                        }
                    });
                });

                // Sum up all weeks
                angular.forEach($scope.myDataAllWeeks, function(valueWeek, keyWeek) {

                    // Initialize with 0
                    $scope.myDataWeekly.push({task : valueItem, totalTime : 0, weekStart : valueWeek});

                    angular.forEach($scope.myData, function(valueAll, keyAll) {
                        if(valueAll.task == valueItem && valueAll.weekStart == valueWeek)
                        {
                            $scope.myDataWeekly[$scope.myDataWeekly.length - 1].totalTime = parseInt($scope.myDataWeekly[$scope.myDataWeekly.length - 1].totalTime,10) + parseInt(valueAll.totalTime,10);
                        }
                    });
                });

                // Sum up all months
                angular.forEach($scope.myDataAllMonths, function(valueMonth, keyMonth) {

                    // Initialize with 0
                    $scope.myDataMonthly.push({task : valueItem, totalTime : 0, monthStart : valueMonth});

                    angular.forEach($scope.myData, function(valueAll, keyAll) {
                        if(valueAll.task == valueItem && valueAll.monthStart == valueMonth)
                        {
                            $scope.myDataMonthly[$scope.myDataMonthly.length - 1].totalTime = parseInt($scope.myDataMonthly[$scope.myDataMonthly.length - 1].totalTime,10) + parseInt(valueAll.totalTime,10);
                        }
                    });
                });  
            });

            resolve($scope.myData);
        }, function errorCallback(response) {
            reject(['Failed to get data from web service']);
        });
    });


    $scope.tableParams = new ngTableParams({
        sorting: {
            totalTime: 'asc'
        }},
                                           {
        getData: function($defer, params) {
            var promise = $scope.getAll();
            promise.then(function(response) {
                $scope.myData = $filter('orderBy')($scope.myData, params.orderBy());
                $defer.resolve($scope.myData);
            });
        }
    });

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

    .filter('secondsToDateTime', [function() {
        return function(seconds) {
            return new Date(1970, 0, 1).setSeconds(seconds);
        };
    }])

;


    