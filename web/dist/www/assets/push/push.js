(function () {

    var es = new EventSource("/event-stream/");
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
        var dynamicGames = document.querySelector("#dynamic-games");
        if (!dynamicGames) return;
        for (var serverName in servers) {
            var server = servers[serverName];
            var updatedTime = new Date(server.updatedTime);
            var remove = (new Date() - updatedTime) > 30000;
            var id = "server-" + serverName.hashCode();
            var existingServer = document.querySelector("#" + id);
            if (remove && existingServer) {
                existingServer.parentNode.removeChild(existingServer);
            } else if (!existingServer) {
                var newDiv = document.createElement("div");
                newDiv.setAttribute("id", id);
                newDiv.innerHTML = server.html;
                dynamicGames.appendChild(newDiv);
            } else {
                existingServer.innerHTML = server.html;
            }
        }
    }

    es.addEventListener("current-game-status-fragment", function (messageEvent) {
        var serverStatus = JSON.parse(messageEvent.data);
        var serverName = serverStatus.now.server.server;
        servers[serverName] = serverStatus;
        update_servers();
    }, false);

    es.addEventListener("new-game", function (event) {
        var newGames = document.querySelector("#new-games");
        if (!newGames) return;
        var game = JSON.parse(event.data);
        var gameId = game.id;
        var divId = "new-game_" + gameId.hashCode();
        var existingDiv = document.querySelector("#" + divId);
        if (!existingDiv) {
            var newDiv = document.createElement("div");
            newDiv.setAttribute("id", divId);
            newDiv.innerHTML = game.html;
            newGames.insertBefore(newDiv, newGames.firstChild);
        }
    });

})();


