import { ITableParamsConstructor, INgTableParams } from 'ng-table';

const templateUrl = require('./my-table.component.html');

interface Person {
    name: string;
    age: number;
}

class MyTableController {
    tableParams: INgTableParams<Person>;
    static $inject = ['NgTableParams'];
    constructor(NgTableParams: ITableParamsConstructor<Person>) {
        var data = [
            { name: "Moroni", age: 50 },
            { name: "Tiancum", age: 43 },
            { name: "Jacob", age: 27 },
            { name: "Nephi", age: 29 },
            { name: "Enos", age: 34 },
            { name: "Tiancum", age: 43 },
            { name: "Jacob", age: 27 },
            { name: "Nephi", age: 29 },
            { name: "Enos", age: 34 },
            { name: "Tiancum", age: 43 },
            { name: "Jacob", age: 27 },
            { name: "Nephi", age: 29 },
            { name: "Enos", age: 34 },
            { name: "Tiancum", age: 43 },
            { name: "Jacob", age: 27 },
            { name: "Nephi", age: 29 },
            { name: "Enos", age: 34 }
        ];

        this.tableParams = new NgTableParams({}, {
            dataset: data
        });
    }
}

const myTableComponent = {
    templateUrl: templateUrl,
    controller: MyTableController
};

export { myTableComponent };