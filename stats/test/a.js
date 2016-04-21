process.env.NODE_ENV = 'test';

var chai = require('chai');
var d3 = require("d3");
var stater = require("../stater").stater(d3);
chai.should();

describe("k", function () {
    var data = [
        "2016-04-10T05:11:29Z",
        "2016-04-10T06:21:29Z",
        "2016-04-12T18:11:29Z",
        "2016-04-21T18:11:29Z"
    ];
    it("Should show proper mapping", function () {
        stater(data).displayDates.should.eql([
            new Date("2016-04-21T16:00:00Z"),
            new Date("2016-04-20T16:00:00Z"),
            new Date("2016-04-19T16:00:00Z"),
            new Date("2016-04-18T16:00:00Z"),
            new Date("2016-04-17T16:00:00Z"),
            new Date("2016-04-16T16:00:00Z"),
            new Date("2016-04-15T16:00:00Z"),
            new Date("2016-04-14T16:00:00Z"),
            new Date("2016-04-13T16:00:00Z"),
            new Date("2016-04-12T16:00:00Z"),
            new Date("2016-04-11T16:00:00Z"),
            new Date("2016-04-10T16:00:00Z")
        ]);
    });
    it("Should get stuff out properly", function () {
        stater(data).mappings.should.eql([11, 11, 9, 0]);
    });
});