<?php
require_once("../render.inc.php");
require("../render_game.inc.php");
?>
    <article id="questions">
        <h1>API</h1>
        <p>All the data accessible via the website is freely accessible via the API.
        See <a href="https://github.com/ScalaWilliam/ActionFPS/tree/master/www" target="_blank">the website source code</a>
        for some examples.</a></a></p>
        
        <h2>Query the most recent games</h2>
        <pre><code><a href="http://api.actionfps.com/recent/" rel="nofollow" target="_blank">http://api.actionfps.com/recent/</a></code></pre>

        <h2>Query clans</h2>
        <pre><code><a href="http://api.actionfps.com/clans/json/" rel="nofollow" target="_blank">http://api.actionfps.com/clans/json/</a></code></pre>
        <pre><code><a href="http://api.actionfps.com/clans/yaml/" rel="nofollow" target="_blank">http://api.actionfps.com/clans/yaml/</a></code></pre>

        <h2>Query users</h2>
        <pre><code><a href="http://api.actionfps.com/users/" rel="nofollow" target="_blank">http://api.actionfps.com/users/</a></code></pre>

        <h2>Query a full user profile</h2>
        <pre><code><a href="http://api.actionfps.com/user/lozi/full/" rel="nofollow" target="_blank">http://api.actionfps.com/user/lozi/full/</a></code></pre>

        <h2>Query <span style="text-decoration: underline;">ALL GAMES</span></h2>
        <p>This is a goldmine for data exploration</p>
        <pre><code><a href="http://api.actionfps.com/raw/" rel="nofollow" target="_blank">http://api.actionfps.com/raw/</a></code></pre>
        
    </article>

<?php echo $foot; ?>
