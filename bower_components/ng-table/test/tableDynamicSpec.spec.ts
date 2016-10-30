import { IAugmentedJQuery, ICompileService, IQService, IScope } from 'angular';
import * as ng1 from 'angular';
import { ngTable } from '../index';
import { INgTableParams, ITableParamsConstructor } from '../src/core';
import { ColumnFieldContext, DynamicTableColField, IDynamicTableColDef, IFilterTemplateDefMap, ISelectOption } from '../src/browser';

describe('ng-table-dynamic', () => {

    interface IPerson {
        id?: number;
        name?: string;
        age: number;
        money?: number;
    }



    interface IExtendedDynamicTableColDef extends IDynamicTableColDef {
        field: DynamicTableColField<string>
}

    interface ICustomizedScope extends IScope {
        tableParams: INgTableParams<IPerson>;
        cols: IExtendedDynamicTableColDef[];
    }

    var dataset = [
        { id: 1, name: "Moroni", age: 50, money: -10 },
        { id: 2, name: "Tiancum", age: 43, money: 120 },
        { id: 3, name: "Jacob", age: 27, money: 5.5 },
        { id: 4, name: "Nephi", age: 29, money: -54 },
        { id: 5, name: "Enos", age: 34, money: 110 },
        { id: 6, name: "Tiancum", age: 43, money: 1000 },
        { id: 7, name: "Jacob", age: 27, money: -201 },
        { id: 8, name: "Nephi", age: 29, money: 100 },
        { id: 9, name: "Enos", age: 34, money: -52.5 },
        { id: 10, name: "Tiancum", age: 43, money: 52.1 },
        { id: 11, name: "Jacob", age: 27, money: 110 },
        { id: 12, name: "Nephi", age: 29, money: -55 },
        { id: 13, name: "Enos", age: 34, money: 551 },
        { id: 14, name: "Tiancum", age: 43, money: -1410 },
        { id: 15, name: "Jacob", age: 27, money: 410 },
        { id: 16, name: "Nephi", age: 29, money: 100 },
        { id: 17, name: "Enos", age: 34, money: -100 }
    ];

    beforeAll(() => expect(ngTable).toBeDefined());
    beforeEach(ng1.mock.module('ngTable'));

    var scope: ICustomizedScope;
    beforeEach(inject(($rootScope: IScope) => {
        scope = $rootScope.$new(true) as ICustomizedScope;
    }));

    describe('basics', () => {
        var elm: IAugmentedJQuery;
        beforeEach(inject(function ($compile: ICompileService, $q: IQService, NgTableParams: ITableParamsConstructor<any>) {
            elm = ng1.element(
                '<div>' +
                '<table ng-table-dynamic="tableParams with cols">' +
                '<tr ng-repeat="user in $data">' +
                '<td ng-repeat="col in $columns">{{user[col.field]}}</td>' +
                '</tr>' +
                '</table>' +
                '</div>');

            function getCustomClass(context: ColumnFieldContext) {
                if (context.$column.title().indexOf('Money') !== -1) {
                    return 'moneyHeaderClass';
                } else {
                    return 'customClass';
                }
            }

            function money(context: ColumnFieldContext) {
                let selectOptions = [{
                    'id': 10,
                    'title': '10'
                }];
                return $q.when(selectOptions);
            }

            scope.tableParams = new NgTableParams({}, {});
            scope.cols = [
                {
                    'class': getCustomClass,
                    field: 'name',
                    filter: { ['name']: 'text' },
                    headerTitle: 'Sort by Name',
                    sortable: 'name',
                    show: true,
                    title: 'Name of person'
                },
                {
                    'class': getCustomClass,
                    field: 'age',
                    headerTitle: 'Sort by Age',
                    sortable: 'age',
                    show: true,
                    title: 'Age'
                },
                {
                    'class': getCustomClass,
                    field: 'money',
                    filter: { ['action']: 'select' },
                    headerTitle: 'Sort by Money',
                    filterData: money,
                    show: true,
                    title: 'Money'
                }
            ];

            $compile(elm)(scope);
            scope.$digest();
        }));

        it('should create table header', () => {
            var thead = elm.find('thead');
            expect(thead.length).toBe(1);

            var rows = thead.find('tr');
            expect(rows.length).toBe(2);

            var titles = ng1.element(rows[0]).find('th');

            expect(titles.length).toBe(3);
            expect(ng1.element(titles[0]).text().trim()).toBe('Name of person');
            expect(ng1.element(titles[1]).text().trim()).toBe('Age');
            expect(ng1.element(titles[2]).text().trim()).toBe('Money');

            expect(ng1.element(rows[1]).hasClass('ng-table-filters')).toBeTruthy();
            var filters = ng1.element(rows[1]).find('th');
            expect(filters.length).toBe(3);
            expect(ng1.element(filters[0]).hasClass('filter')).toBeTruthy();
            expect(ng1.element(filters[1]).hasClass('filter')).toBeTruthy();
            expect(ng1.element(filters[2]).hasClass('filter')).toBeTruthy();
        });

        it('should create table header classes', inject(function ($compile: ICompileService, $rootScope: IScope) {

            var thead = elm.find('thead');
            var rows = thead.find('tr');
            var titles = ng1.element(rows[0]).find('th');

            expect(ng1.element(titles[0]).hasClass('header')).toBeTruthy();
            expect(ng1.element(titles[1]).hasClass('header')).toBeTruthy();
            expect(ng1.element(titles[2]).hasClass('header')).toBeTruthy();

            expect(ng1.element(titles[0]).hasClass('sortable')).toBeTruthy();
            expect(ng1.element(titles[1]).hasClass('sortable')).toBeTruthy();
            expect(ng1.element(titles[2]).hasClass('sortable')).toBeFalsy();

            expect(ng1.element(titles[0]).hasClass('customClass')).toBeTruthy();
            expect(ng1.element(titles[1]).hasClass('customClass')).toBeTruthy();
            expect(ng1.element(titles[2]).hasClass('moneyHeaderClass')).toBeTruthy();
        }));

        it('should create table header titles', () => {

            var thead = elm.find('thead');
            var rows = thead.find('tr');
            var titles = ng1.element(rows[0]).find('th');

            expect(ng1.element(titles[0]).attr('title').trim()).toBe('Sort by Name');
            expect(ng1.element(titles[1]).attr('title').trim()).toBe('Sort by Age');
            expect(ng1.element(titles[2]).attr('title').trim()).toBe('Sort by Money');
        });

        it('should show data-title-text', inject(function (NgTableParams: ITableParamsConstructor<IPerson>) {
            var tbody = elm.find('tbody');

            scope.tableParams = new NgTableParams({
                page: 1, // show first page
                count: 10 // count per page
            }, {
                    dataset: dataset
                });
            scope.$digest();

            var filterRow = ng1.element(elm.find('thead').find('tr')[1]);
            var filterCells = filterRow.find('th');
            expect(ng1.element(filterCells[0]).attr('data-title-text').trim()).toBe('Name of person');
            expect(ng1.element(filterCells[1]).attr('data-title-text').trim()).toBe('Age');
            expect(ng1.element(filterCells[2]).attr('data-title-text').trim()).toBe('Money');

            var dataRows = elm.find('tbody').find('tr');
            var dataCells = ng1.element(dataRows[0]).find('td');
            expect(ng1.element(dataCells[0]).attr('data-title-text').trim()).toBe('Name of person');
            expect(ng1.element(dataCells[1]).attr('data-title-text').trim()).toBe('Age');
            expect(ng1.element(dataCells[2]).attr('data-title-text').trim()).toBe('Money');
        }));

        it('should show/hide columns', inject(function (NgTableParams: ITableParamsConstructor<IPerson>) {
            var tbody = elm.find('tbody');

            scope.tableParams = new NgTableParams({
                page: 1, // show first page
                count: 10 // count per page
            }, {
                    dataset: dataset
                });
            scope.$digest();

            var headerRow = ng1.element(elm.find('thead').find('tr')[0]);
            expect(headerRow.find('th').length).toBe(3);

            var filterRow = ng1.element(elm.find('thead').find('tr')[1]);
            expect(filterRow.find('th').length).toBe(3);

            var dataRow = ng1.element(elm.find('tbody').find('tr')[0]);
            expect(dataRow.find('td').length).toBe(3);

            scope.cols[0].show = false;
            scope.$digest();
            expect(headerRow.find('th').length).toBe(2);
            expect(filterRow.find('th').length).toBe(2);
            expect(dataRow.find('td').length).toBe(2);
            expect(ng1.element(headerRow.find('th')[0]).text().trim()).toBe('Age');
            expect(ng1.element(headerRow.find('th')[1]).text().trim()).toBe('Money');
            expect(ng1.element(filterRow.find('th')[0]).find('input').length).toBe(0);
            expect(ng1.element(filterRow.find('th')[1]).find('select').length).toBe(1);
        }));
    });
    describe('changing column list', () => {
        var elm: IAugmentedJQuery;
        beforeEach(inject(function ($compile: ICompileService, $q: IQService, NgTableParams: ITableParamsConstructor<IPerson>) {
            elm = ng1.element(
                '<div>' +
                '<table ng-table-dynamic="tableParams with cols">' +
                '<tr ng-repeat="user in $data">' +
                '<td ng-repeat="col in $columns">{{user[col.field]}}</td>' +
                '</tr>' +
                '</table>' +
                '</div>');

            function getCustomClass(parmasScope: ColumnFieldContext) {
                if (parmasScope.$column.title().indexOf('Money') !== -1) {
                    return 'moneyHeaderClass';
                } else {
                    return 'customClass';
                }
            }

            function money(/*$column*/) {
                var def = $q.defer();
                def.resolve([{
                    'id': 10,
                    'title': '10'
                }]);
                return def;
            }

            scope.tableParams = new NgTableParams({}, {});
            scope.cols = [
                {
                    'class': getCustomClass,
                    field: 'name',
                    filter: { name: 'text' },
                    headerTitle: 'Sort by Name',
                    sortable: 'name',
                    show: true,
                    title: 'Name of person'
                },
                {
                    'class': getCustomClass,
                    field: 'age',
                    headerTitle: 'Sort by Age',
                    sortable: 'age',
                    show: true,
                    title: 'Age'
                }
            ];

            $compile(elm)(scope);
            scope.$digest();
        }));

        it('adding new column should update table header', () => {
            var newCol: IExtendedDynamicTableColDef = {
                'class': 'moneyadd',
                field: 'money',
                filter: { action: 'select' },
                headerTitle: 'Sort by Money',
                show: true,
                title: 'Money'
            };
            scope.cols.push(newCol);
            scope.$digest();
            var thead = elm.find('thead');
            expect(thead.length).toBe(1);

            var rows = thead.find('tr');
            expect(rows.length).toBe(2);

            var titles = ng1.element(rows[0]).find('th');

            expect(titles.length).toBe(3);
            expect(ng1.element(titles[0]).text().trim()).toBe('Name of person');
            expect(ng1.element(titles[1]).text().trim()).toBe('Age');
            expect(ng1.element(titles[2]).text().trim()).toBe('Money');

            var filterRow = ng1.element(rows[1]);
            expect(filterRow.hasClass('ng-table-filters')).toBeTruthy();
            expect(filterRow.hasClass("ng-hide")).toBe(false);

            var filters = filterRow.find('th');
            expect(filters.length).toBe(3);
            expect(ng1.element(filters[0]).hasClass('filter')).toBeTruthy();
            expect(ng1.element(filters[1]).hasClass('filter')).toBeTruthy();
            expect(ng1.element(filters[2]).hasClass('filter')).toBeTruthy();

        });

        it('removing new column should update table header', () => {
            scope.cols.splice(0, 1);
            scope.$digest();
            var thead = elm.find('thead');
            expect(thead.length).toBe(1);

            var rows = thead.find('tr');
            var titles = ng1.element(rows[0]).find('th');
            expect(titles.length).toBe(1);
            expect(ng1.element(titles[0]).text().trim()).toBe('Age');

            var filterRow = ng1.element(rows[1]);
            expect(filterRow.hasClass("ng-hide")).toBe(true);
        });

        it('setting columns to null should remove all table columns from header', () => {
            scope.cols = null;
            scope.$digest();
            var thead = elm.find('thead');
            expect(thead.length).toBe(1);

            var rows = thead.find('tr');
            var titles = ng1.element(rows[0]).find('th');
            expect(titles.length).toBe(0);

            var filterRow = ng1.element(rows[1]);
            expect(filterRow.hasClass("ng-hide")).toBe(true);

            expect(filterRow.find('th').length).toBe(0);
        });

    });
    describe('title-alt', () => {

        var elm: IAugmentedJQuery;
        beforeEach(inject(function ($compile: ICompileService, NgTableParams: ITableParamsConstructor<IPerson>) {
            elm = ng1.element(
                '<table ng-table-dynamic="tableParams with cols">' +
                '<tr ng-repeat="user in $data">' +
                '<td ng-repeat="col in $columns">{{user[col.field]}}</td>' +
                '</tr>' +
                '</table>');

            scope.cols = [
                { field: 'name', title: 'Name of person', titleAlt: 'Name' },
                { field: 'age', title: 'Age', titleAlt: 'Age' },
                { field: 'money', title: 'Money', titleAlt: '£' }
            ];
            scope.tableParams = new NgTableParams({
                page: 1, // show first page
                count: 10 // count per page
            }, {
                    dataset: dataset
                });

            $compile(elm)(scope);
            scope.$digest();
        }));

        it('should show as data-title-text', inject(function ($compile: ICompileService) {
            var filterRow = ng1.element(elm.find('thead').find('tr')[1]);
            var filterCells = filterRow.find('th');

            expect(ng1.element(filterCells[0]).attr('data-title-text').trim()).toBe('Name');
            expect(ng1.element(filterCells[1]).attr('data-title-text').trim()).toBe('Age');
            expect(ng1.element(filterCells[2]).attr('data-title-text').trim()).toBe('£');

            var dataRows = elm.find('tbody').find('tr');
            var dataCells = ng1.element(dataRows[0]).find('td');
            expect(ng1.element(dataCells[0]).attr('data-title-text').trim()).toBe('Name');
            expect(ng1.element(dataCells[1]).attr('data-title-text').trim()).toBe('Age');
            expect(ng1.element(dataCells[2]).attr('data-title-text').trim()).toBe('£');
        }));
    });

    describe('filters', () => {

        var elm: IAugmentedJQuery;
        beforeEach(inject(function ($compile: ICompileService, NgTableParams: ITableParamsConstructor<IPerson>) {
            elm = ng1.element(
                '<table ng-table-dynamic="tableParams with cols">' +
                '<tr ng-repeat="user in $data">' +
                '<td ng-repeat="col in $columns">{{user[col.field]}}</td>' +
                '</tr>' +
                '</table>');
        }));

        describe('filter specified as alias', () => {

            beforeEach(inject(function ($compile: ICompileService, NgTableParams: ITableParamsConstructor<IPerson>) {
                scope.cols = [
                    { field: 'name', filter: { username: 'text' } }
                ];
                scope.tableParams = new NgTableParams({}, {});
                $compile(elm)(scope);
                scope.$digest();
            }));

            it('should render named filter template', () => {
                var inputs = elm.find('thead').find('tr').eq(1).find('th').find('input');
                expect(inputs.length).toBe(1);
                expect(inputs.eq(0).attr('type')).toBe('text');
                expect(inputs.eq(0).attr('ng-model')).not.toBeUndefined();
                expect(inputs.eq(0).attr('name')).toBe('username');
            });

            it('should render named filter template - select template', () => {
                var inputs = elm.find('thead').find('tr').eq(1).find('th').find('input');
                expect(inputs.length).toBe(1);
                expect(inputs.eq(0).attr('type')).toBe('text');
                expect(inputs.eq(0).attr('ng-model')).not.toBeUndefined();
                expect(inputs.eq(0).attr('name')).toBe('username');
            });

            it('should databind ngTableParams.filter to filter input', () => {
                scope.tableParams.filter()['username'] = 'my name is...';
                scope.$digest();

                var input = elm.find('thead').find('tr').eq(1).find('th').find('input');
                expect(input.val()).toBe('my name is...');
            });
        });

        describe('select filter', () => {

            beforeEach(inject(function ($compile: ICompileService, $q: IQService, NgTableParams: ITableParamsConstructor<IPerson>) {
                scope.cols = [{
                    field: 'name',
                    filter: { username: 'select' },
                    filterData: getNamesAsDefer
                }, {
                        field: 'names2',
                        filter: { username2: 'select' },
                        filterData: getNamesAsPromise
                    }, {
                        field: 'names3',
                        filter: { username3: 'select' },
                        filterData: getNamesAsArray
                    }];
                scope.tableParams = new NgTableParams({}, {});
                $compile(elm)(scope);
                scope.$digest();

                function getNamesAsDefer(/*$column*/) {
                    return $q.when([{
                        'id': 10,
                        'title': 'Christian'
                    }, {
                            'id': 11,
                            'title': 'Simon'
                        }]);
                }

                function getNamesAsPromise(/*$column*/) {
                    return $q.when([{
                        'id': 20,
                        'title': 'Christian'
                    }, {
                            'id': 21,
                            'title': 'Simon'
                        }]);
                }

                function getNamesAsArray(/*$column*/) {
                    return [{
                        'id': 20,
                        'title': 'Christian'
                    }, {
                            'id': 21,
                            'title': 'Simon'
                        }];
                }

            }));

            it('should render select lists', () => {
                var inputs = elm.find('thead').find('tr').eq(1).find('th').find('select');
                expect(inputs.length).toBe(3);
                expect(inputs.eq(0).attr('ng-model')).not.toBeUndefined();
                expect(inputs.eq(0).attr('name')).toBe('username');
                expect(inputs.eq(1).attr('ng-model')).not.toBeUndefined();
                expect(inputs.eq(1).attr('name')).toBe('username2');
                expect(inputs.eq(2).attr('ng-model')).not.toBeUndefined();
                expect(inputs.eq(2).attr('name')).toBe('username3');
            });

            it('should render select list return as a promise', () => {
                var inputs = elm.find('thead').find('tr').eq(1).find('th').eq(1).find('select');
                var select = inputs.eq(0) as IAugmentedJQuery;
                expect((select[0] as HTMLSelectElement).options.length).toBeGreaterThan(0);
                var $column = (select.scope() as ColumnFieldContext).$column;
                var plucker = _.partialRight(_.pick, ['id', 'title']);
                var actual = _.map($column.data as ISelectOption[], plucker);
                expect(actual).toEqual([{
                    'id': '',
                    'title': ''
                }, {
                        'id': 20,
                        'title': 'Christian'
                    }, {
                        'id': 21,
                        'title': 'Simon'
                    }]);
            });

            it('should render select list return as an array', () => {
                var inputs = elm.find('thead').find('tr').eq(1).find('th').eq(2).find('select');
                var select = inputs.eq(0) as IAugmentedJQuery;
                expect((select[0] as HTMLSelectElement).options.length).toBeGreaterThan(0);
                var $column = (select.scope() as ColumnFieldContext).$column;
                var plucker = _.partialRight(_.pick, ['id', 'title']);
                var actual = _.map($column.data as ISelectOption[], plucker);
                expect(actual).toEqual([{
                    'id': '',
                    'title': ''
                }, {
                        'id': 20,
                        'title': 'Christian'
                    }, {
                        'id': 21,
                        'title': 'Simon'
                    }]);
            });
        });

        describe('multiple filter inputs', () => {

            beforeEach(inject(function ($compile: ICompileService, NgTableParams: ITableParamsConstructor<IPerson>) {
                scope.cols = [
                    { field: 'name', filter: { name: 'text', age: 'text' } }
                ];
                scope.tableParams = new NgTableParams({}, {});
                $compile(elm)(scope);
                scope.$digest();
            }));

            it('should render filter template for each key/value pair ordered by key', () => {
                var inputs = elm.find('thead').find('tr').eq(1).find('th').find('input');
                expect(inputs.length).toBe(2);
                expect(inputs.eq(0).attr('type')).toBe('text');
                expect(inputs.eq(0).attr('ng-model')).not.toBeUndefined();
                expect(inputs.eq(1).attr('type')).toBe('text');
                expect(inputs.eq(1).attr('ng-model')).not.toBeUndefined();
            });

            it('should databind ngTableParams.filter to filter inputs', () => {
                scope.tableParams.filter()['name'] = 'my name is...';
                scope.tableParams.filter()['age'] = '10';
                scope.$digest();

                var inputs = elm.find('thead').find('tr').eq(1).find('th').find('input');
                expect(inputs.eq(0).val()).toBe('my name is...');
                expect(inputs.eq(1).val()).toBe('10');
            });
        });

        describe('dynamic filter', () => {

            var ageFilter: IFilterTemplateDefMap;
            beforeEach(inject(function ($compile: ICompileService, NgTableParams: ITableParamsConstructor<IPerson>) {

                ageFilter = { age: 'text' };
                function getFilter(paramsScope: ColumnFieldContext): IFilterTemplateDefMap {
                    if (paramsScope.$column.title() === 'Name of user') {
                        return { username: 'text' };
                    } else if (paramsScope.$column.title() === 'Age') {
                        return ageFilter;
                    } else {
                        return undefined;
                    }
                }

                scope.cols = [
                    { field: 'name', title: 'Name of user', filter: getFilter },
                    { field: 'age', title: 'Age', filter: getFilter }
                ];
                scope.tableParams = new NgTableParams({}, {});

                $compile(elm)(scope);
                scope.$digest();

            }));

            it('should render named filter template', () => {
                var usernameInput = elm.find('thead').find('tr').eq(1).find('th').eq(0).find('input');
                expect(usernameInput.attr('type')).toBe('text');
                expect(usernameInput.attr('name')).toBe('username');

                var ageInput = elm.find('thead').find('tr').eq(1).find('th').eq(1).find('input');
                expect(ageInput.attr('type')).toBe('text');
                expect(ageInput.attr('name')).toBe('age');
            });

            it('should databind ngTableParams.filter to filter input', () => {
                scope.tableParams.filter()['username'] = 'my name is...';
                scope.tableParams.filter()['age'] = '10';
                scope.$digest();

                var usernameInput = elm.find('thead').find('tr').eq(1).find('th').eq(0).find('input');
                expect(usernameInput.val()).toBe('my name is...');
                var ageInput = elm.find('thead').find('tr').eq(1).find('th').eq(1).find('input');
                expect(ageInput.val()).toBe('10');
            });

            it('should render new template as filter changes', inject(function ($compile: ICompileService) {

                var scriptTemplate = ng1.element(
                    '<script type="text/ng-template" id="ng-table/filters/number.html"><input type="number" name="{{name}}"/></script>');
                $compile(scriptTemplate)(scope);

                ageFilter['age'] = 'number';
                scope.$digest();

                var ageInput = elm.find('thead').find('tr').eq(1).find('th').eq(1).find('input');
                expect(ageInput.attr('type')).toBe('number');
                expect(ageInput.attr('name')).toBe('age');
            }));
        });
    });

    describe('reorder columns', () => {
        var elm: IAugmentedJQuery;
        var getTitles = () => {
            var thead = elm.find('thead');
            var rows = thead.find('tr');
            var titles = ng1.element(rows[0]).find('th');

            return ng1.element(titles).text().trim().split(/\s+/g)
        };

        beforeEach(inject(function ($compile: ICompileService, $q: IQService, NgTableParams: ITableParamsConstructor<IPerson>) {
            elm = ng1.element(
                '<div>' +
                '<table ng-table-dynamic="tableParams with cols">' +
                '<tr ng-repeat="user in $data">' +
                "<td ng-repeat=\"col in $columns\">{{user[col.field]}}</td>" +
                '</tr>' +
                '</table>' +
                '</div>');

            scope.tableParams = new NgTableParams({}, {});
            scope.cols = [
                {
                    field: 'name',
                    title: 'Name'
                },
                {
                    field: 'age',
                    title: 'Age'
                },
                {
                    field: 'money',
                    title: 'Money'
                }
            ];

            $compile(elm)(scope);
            scope.$digest();
        }));

        it('"in place" switch of columns within array should reorder html table columns', () => {
            expect(getTitles()).toEqual(['Name', 'Age', 'Money']);

            var colToSwap = scope.cols[2];
            scope.cols[2] = scope.cols[1];
            scope.cols[1] = colToSwap;
            scope.$digest();

            expect(getTitles()).toEqual(['Name', 'Money', 'Age']);
        });

        it('"in place" reverse of column array should reorder html table columns', () => {
            expect(getTitles()).toEqual(['Name', 'Age', 'Money']);

            scope.cols.reverse();
            scope.$digest();

            expect(getTitles()).toEqual(['Money', 'Age', 'Name']);
        });

        it('html table columns should reflect order of columns in replacement array', () => {
            expect(getTitles()).toEqual(['Name', 'Age', 'Money']);

            var newArray = scope.cols.map(ng1.identity);
            newArray.reverse();
            scope.cols = newArray;
            scope.$digest();

            expect(getTitles()).toEqual(['Money', 'Age', 'Name']);
        });
    });
});