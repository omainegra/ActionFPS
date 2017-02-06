(function () {

    var es = new EventSource("/server-updates/");

    es.addEventListener("current-game-status-fragment", function (messageEvent) {
        var serverStatus = JSON.parse(messageEvent.data);
        var serverName = serverStatus.now.server.connectName;
        var theServer = document.querySelector(".server[data-server='" + serverName + "']");
        var gameStatus = theServer.querySelector(".game-status");
        while (gameStatus.hasChildNodes())
            gameStatus.removeChild(gameStatus.firstChild);
        if (serverStatus.map && serverStatus.mode) {
            gameStatus.appendChild(document.createTextNode(serverStatus.mode + " @ " + serverStatus.map));
            gameStatus.appendChild(document.createElement("br"));
            gameStatus.appendChild(document.createTextNode("Remaining: " + serverStatus.minRemain + "m"));
            var target = document.querySelector("#join[data-server='"+serverStatus.now.server.server+"'] #server-status");

            if ( target ) {
                target.classList.add("active");
                target.innerHTML = serverStatus.html;
            }
        }
        console.log(serverStatus, theServer);
    }, false);

})();


