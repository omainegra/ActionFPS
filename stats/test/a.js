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
            "2016-04-21",
            "2016-04-12",
            "2016-04-10"
        ]);
    });

    it("Should get stuff out properly", function () {
        stater(data).mappings.should.eql([2, 2, 1, 0]);
    });

    it("Should do a proper rendering", function() {
        var thing = stater(data).render(2400, 200, 100);
        data.map(thing.xPosition).should.eql([519, 636, 1819, 1819]);
        data.map(thing.yPosition).should.eql([200, 200, 100, 0]);
    })

});