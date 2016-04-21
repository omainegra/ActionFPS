//noinspection ThisExpressionReferencesGlobalObjectJS
(function () {
    "use strict";

    this.stater = function (d3) {
        return function (datums) {
            var dds = displayDates();

            return {
                displayDates: displayDates(),
                mappings: mappings(),
                render: render
            };

            function displayDates() {
                return Object.keys(datums.map(function (id) {
                        return id.substr(0, 10);
                    }).reduce(function (accum, n) {
                        accum[n] = true;
                        return accum;
                    }, {}))
                    .sort()
                    .reverse();
            }

            /**
             * Return index corresponding to date field
             */
            function mappings() {
                return datums.map(function (val) {
                    return displayDates().indexOf(val.substr(0, 10));
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
                        return itemHeight * dds.indexOf(dayOfDate(id));
                    },
                    xPosition: function (id) {
                        return Math.round(scaleX(timeOfDate(id)));
                    }
                }
            }


        };
    };
}).call(this);