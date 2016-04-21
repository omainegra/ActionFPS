//noinspection ThisExpressionReferencesGlobalObjectJS
(function () {
    "use strict";

    this.stater = function (d3) {
        return function (datums) {
            return {
                displayDates: displayDates(),
                mappings: mappings()
            };

            function dates() {
                return datums.map(function (x) {
                    return new Date(x);
                });
            }

            function displayDates() {
                return d3.time.scale
                    .utc()
                    .domain([d3.min(dates()), d3.max(dates())])
                    .ticks(d3.time.days, 1)
                    .reverse();
            }

            /**
             * Return index corresponding to date field
             */
            function mappings() {
                var scl = d3.time.scale()
                    .domain([d3.min(dates()), d3.max(dates())])
                    .nice()
                    .range([displayDates().length, 0]);
                return dates().map(scl).map(function (k) {
                    /** Ugly but works - how do we do this in d3? **/
                    return Math.floor(k);
                });
            }
        };
    };
}).call(this);