<?php
require_once("../render.inc.php");
require("../render_game.inc.php");
?>
    <article id="questions">
        <h2>Query the most recent games</h2>
        <pre><code><a href="http://api.actionfps.com/recent/" rel="nofollow" target="_blank">http://api.actionfps.com/recent/</a></code></pre>

        <h2>Query clans</h2>
        <pre><code><a href="http://api.actionfps.com/clans/json/" rel="nofollow" target="_blank">http://api.actionfps.com/clans/json/</a></code></pre>
        <pre><code><a href="http://api.actionfps.com/clans/yaml/" rel="nofollow" target="_blank">http://api.actionfps.com/clans/yaml/</a></code></pre>

        <h2>Query users</h2>
        <pre><code><a href="http://api.actionfps.com/users/" rel="nofollow" target="_blank">http://api.actionfps.com/users/</a></code></pre>

        <h2>Query <span style="text-decoration: underline;">ALL GAMES</span></h2>
        <p>This is a goldmine for data exploration</p>
        <pre><code><a href="http://api.actionfps.com/raw/" rel="nofollow" target="_blank">http://api.actionfps.com/raw/</a></code></pre>

    </article>

<?php echo $foot; ?>