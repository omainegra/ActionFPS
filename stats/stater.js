//noinspection ThisExpressionReferencesGlobalObjectJS
(function () {
    "use strict";

    this.stater = function (d3) {
        return function (datums) {
            var dds = displayDates();

            return {
                displayDates: displayDates(),
                mappings: mappings(),
                table: table(),
                render: render
            };

            function displayDates() {
                var coll = [];
                datums.forEach(function(d) {
                    var date = d.substr(0, 10);
                    if ( coll.indexOf(date) == -1 ) {
                        coll.push(date);
                    }
                });
                coll.sort().reverse();
                return coll;
            }

            function dateToDatetimes() {
                var map = {};
                datums.map(function(date) {
                    var d = date.substr(0, 10);
                    if ( !(d in map) ) {
                        map[d] = [];
                    }
                    map[d].push(date)
                });
                return map;
            }

            function table() {
                var dtd = dateToDatetimes();
                return displayDates().map(function(displayDate) {
                    return {
                        displayDate: displayDate,
                        dateTimes: dtd[displayDate]
                    };
                })
            }

            /**
             * Return index corresponding to date field
             */
            function mappings() {
                return datums.map(function (val) {
                    return dds.indexOf(val.substr(0, 10));
                });
            }

            function dayOfDate(date) {
                return date.substr(0, 10);
            }

            function timeOfDate(date) {
                return ((new Date(date).getTime()) - (new Date(date.substring(0, 10)).getTime())) / 1000
            }


            function render(width, height, itemHeight) {
                var scaleX = d3.scale.linear()
                    .domain([0, 3600 * 24])
                    .range([0, width]);
                return {
                    yPosition: function (id) {
                        return itemHeight / 2;
                    },
                    xPosition: function (id) {
                        return Math.round(scaleX(timeOfDate(id)));
                    }
                }
            }


        };
    };
}).call(this);