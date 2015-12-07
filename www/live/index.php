<script type="text/javascript">
    var es = new EventSource("http://api.actionfps.com/server-updates/");
    es.on("current-game-status", function(x) {
        console.log("Got: ", x);
    })
</script>