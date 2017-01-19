(function() {

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
        if ( $("#dynamic-games").length == 0 ) return;
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

    es.addEventListener("new-game", function (event) {
        if ( $("#new-games").length != 0 ) {
            var game = JSON.parse(event.data);
            var gameId = game.id;
            var divId = "new-game_" + gameId.hashCode();
            if ($("#" + divId).length == 0) {
                var newDiv = $("<div id=\"" + divId + "\"></div>").html(game.html);
                $("#new-games").prepend(newDiv);
            }
        }
    });


    if ( !("Notification" in window) ) return;
    Notification.requestPermission();
    es.addEventListener("inter", function(event) {
        showNotification(JSON.parse(event.data));
    });

    function showNotification(json) {
        var options = {
            icon: 'https://assault.cubers.net/docs/images/ac_knife.gif',
            body: json.playerName + ' calls an inter. Click to join!',
            requireInteraction: true
        };
        var notification = new Notification("Inter on " + json.serverName.toUpperCase(), options);
        notification.onclick = function() {
            notification.close();
            window.open(json.serverConnect);
        }
    }

    //showNotification({
    //    name: "w00p|Drakas",
    //    server: "aura.woop.ac:1337"
    //});

})();


