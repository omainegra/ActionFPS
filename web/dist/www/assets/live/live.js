(function () {
    var es = new EventSource("/server-updates/");
    String.prototype.hashCode = function () {
        var hash = 0, i, chr, len;
        if (this.length === 0) return hash;
        for (i = 0, len = this.length; i < len; i++) {
            chr = this.charCodeAt(i);
            hash = ((hash << 5) - hash) + chr;
            hash |= 0; // Convert to 32bit integer
        }
        return hash;
    };
    var servers = {};

    function update_servers() {
        for (var serverName in servers) {
            var server = servers[serverName];
            var updatedTime = new Date(server.updatedTime);
            var remove = (new Date() - updatedTime) > 30000;
            var id = "server-" + serverName.hashCode();
            var existingServer = $("#" + id);
            if (remove) {
                existingServer.remove();
            } else if (existingServer.length == 0) {
                var q = $("<div id=\"" + id + "\"></div>").html(server.html);
                $("#dynamic-games").append(q);
            } else {
                existingServer.html(server.html)
            }
        }
    }

    es.addEventListener("current-game-status-fragment", function (messageEvent) {
        var serverStatus = JSON.parse(messageEvent.data);
        var serverName = serverStatus.now.server.server;
        servers[serverName] = serverStatus;
        update_servers();
    }, false);


    var newGamesSource = new EventSource("/new-games/");
    newGamesSource.addEventListener("new-game", function (event) {
        var game = JSON.parse(event.data);
        var gameId = game.id;
        var divId = "new-game_" + gameId.hashCode();
        if ($("#" + divId).length == 0) {
            var newDiv = $("<div id=\"" + divId + "\"></div>").html(game.html);
            $("#new-games").prepend(newDiv);
        }
    });

})();