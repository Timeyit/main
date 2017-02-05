angular.module('myApp')

    .controller('overviewcontroller', function($scope, $http, $location, $filter, $compile, $window, $interval, UserService, AuthenticationService,$log, uiCalendarConfig, $q) {

    var date = new Date();
    var d = date.getDate();
    var m = date.getMonth();
    var y = date.getFullYear();

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

    $scope.getAll = function()
    {
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
                        if(sdate > $scope.dateStart & sdate < $scope.dateEnd)
                        {
                            $log.log("Event date: " + sdate);
                            var newEvent = {
                                    title: value.idworkItem,
                                    start: new Date(mdate),
                                    end: new Date(mdate + value.Duration*1000),
                                    allDay: false
                            };
                            newEvents.push(newEvent);
                            $scope.events.push(newEvent);
                        }

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

                if(newEvents.length > 0)
                {
                    $log.log('Got new events');
                    $log.log(newEvents);
                }

                resolve(newEvents);
            }, function errorCallback(response) {
                reject('Failed to get data from web service');
            });
        });


    }


    /* event source that contains custom events on the scope */
    $scope.events = [
        
    ];

    /* event source that calls a function on every view switch */
    $scope.eventsF = function (start, end, timezone, callback) {
        $log.log('Generatings eventsF');
        $scope.dateStart = new Date(start).getTime() / 1000;
        $scope.dateEnd = new Date(end).getTime() / 1000;
        var m = new Date(start).getMonth();
        $log.log('Start Date: ' + $scope.dateStart);
        $log.log('End Date: ' + $scope.dateEnd);

        var promise = $scope.getAll();
        promise.then(function(response) {
            if(response.length > 0)
            {
                $log.log('Get events for period. Response: ' + response);
                var events = [{title: 'Feed Me ' + m,start: $scope.dateStart + (50000),end: $scope.dateStart + (100000),allDay: false, className: ['customFeed']}];
                callback(events);
            }
            else
            {
                callback([]);
            }

        }, function(reason) {
            $log.log('Failed to get events for period. Reason: ' + reason);
            callback(events);
        });




    };

    $scope.calEventsExt = {
        color: '#f00',
        textColor: 'yellow',
        events: [ 
            {type:'party',title: 'Lunch',start: new Date(y, m, d, 12, 0),end: new Date(y, m, d, 14, 0),allDay: false},
            {type:'party',title: 'Lunch 2',start: new Date(y, m, d, 12, 0),end: new Date(y, m, d, 14, 0),allDay: false},
            {type:'party',title: 'Click for Google',start: new Date(y, m, 28),end: new Date(y, m, 29),url: 'http://google.com/'}
        ]
    };
    /* alert on eventClick */
    $scope.alertOnEventClick = function( date, jsEvent, view){
        $scope.alertMessage = (date.title + ' was clicked ');
    };
    /* alert on Drop */
    $scope.alertOnDrop = function(event, delta, revertFunc, jsEvent, ui, view){
        $scope.alertMessage = ('Event Droped to make dayDelta ' + delta);
    };
    /* alert on Resize */
    $scope.alertOnResize = function(event, delta, revertFunc, jsEvent, ui, view ){
        $scope.alertMessage = ('Event Resized to make dayDelta ' + delta);
    };
    /* add and removes an event source of choice */
    $scope.addRemoveEventSource = function(sources,source) {
        var canAdd = 0;
        angular.forEach(sources,function(value, key){
            if(sources[key] === source){
                sources.splice(key,1);
                canAdd = 1;
            }
        });
        if(canAdd === 0){
            sources.push(source);
        }
    };
    /* add custom event*/
    $scope.addEvent = function() {
        $scope.events.push({
            title: 'Open Sesame',
            start: new Date(y, m, 28),
            end: new Date(y, m, 29),
            className: ['openSesame']
        });
    };
    /* remove event */
    $scope.remove = function(index) {
        $scope.events.splice(index,1);
    };
    /* Change View */
    $scope.changeView = function(view,calendar) {
        uiCalendarConfig.calendars[calendar].fullCalendar('changeView',view);
    };
    /* Change View */
    $scope.renderCalender = function(calendar)
    {
        if(uiCalendarConfig.calendars[calendar]){
            uiCalendarConfig.calendars[calendar].fullCalendar('render');
        }
    };


    /* Render Tooltip */
    $scope.eventRender = function( event, element, view ) { 
        element.attr({'tooltip': event.title,
                      'tooltip-append-to-body': true});
        $compile(element)($scope);
    };
    /* config object */
    $scope.uiConfig = {
        calendar:{
            height: 450,
            editable: false,
            theme: false,
            defaultView: 'agendaWeek',
            header:{
                left: 'title',
                center: '',
                right: 'today prev,next'
            },
            eventClick: $scope.alertOnEventClick,
            eventDrop: $scope.alertOnDrop,
            eventResize: $scope.alertOnResize,
            eventRender: $scope.eventRender
        }
    };

    $scope.uiConfig.calendar.dayNames = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
    $scope.uiConfig.calendar.dayNamesShort = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

    $scope.changeLang = function() {
        if($scope.changeTo === 'Hungarian'){
            $scope.uiConfig.calendar.dayNames = ["Vasárnap", "Hétfő", "Kedd", "Szerda", "Csütörtök", "Péntek", "Szombat"];
            $scope.uiConfig.calendar.dayNamesShort = ["Vas", "Hét", "Kedd", "Sze", "Csüt", "Pén", "Szo"];
            $scope.changeTo= 'English';
        } else {
            $scope.uiConfig.calendar.dayNames = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
            $scope.uiConfig.calendar.dayNamesShort = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
            $scope.changeTo = 'Hungarian';
        }
    };
    /* event sources array*/
    $scope.eventSources = [$scope.events, $scope.eventsF];
    $scope.eventSources2 = [$scope.calEventsExt, $scope.eventsF, $scope.events];

})
    .filter('secondsToDateTime', [function() {
        return function(seconds) {
            return new Date(1970, 0, 1).setSeconds(seconds);
        };
    }])

;


    