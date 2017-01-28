
function getParameterByName(name, url) {
    if (!url) {
        url = window.location.href;
    }
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}
var join = getParameterByName("join");
if ( join ) {
    $.get("/servers/?format=json").then(function (data) {
        data.filter(function (server) {
            return server.address == join;
        })
            .forEach(function (r) {
                document.querySelector("#servers").classList.add("join");
                document.querySelector("#server-join-link").setAttribute("href", r.url);
                document.querySelector("#server-name").appendChild(document.createTextNode(r.name));
            });
    });
}