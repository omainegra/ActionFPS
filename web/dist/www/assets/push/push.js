(function() {
    if ( !("Notification" in window) ) return;
    Notification.requestPermission();
    var eventStream = new EventSource("/inters/");
    eventStream.addEventListener("inter", function(event) {
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


