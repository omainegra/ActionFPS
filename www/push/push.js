(function() {
    if ( !("Notification" in window) ) return;
    Notification.requestPermission();
    var eventStream = new EventSource("http://api.actionfps.com/inters/");
    eventStream.addEventListener("inter", function(event) {
        showNotification(JSON.parse(event.data));
    });

    function showNotification(json) {
        var serverName = json.server.replace(":", " ").replace(".woop.ac", "");
        var url = "assaultcube://" + json.server;
        var options = {
            icon: 'https://assault.cubers.net/docs/images/ac_knife.gif',
            body: json.name + ' calls an inter. Click to join!'
        };
        var notification = new Notification("Inter on " + serverName.toUpperCase(), options);
        notification.onclick = function() {
            notification.close();
            window.open(url);
        }
    }

    //showNotification({
    //    name: "w00p|Drakas",
    //    server: "aura.woop.ac:1337"
    //});

})();


