<?php
require_once("../render.inc.php");
require("../render_game.inc.php");
?>
    <article id="questions">
        <h1>API</h1>
        <p>All the data accessible via the website is freely accessible via the API.
            <br/>
            See <a href="https://github.com/ScalaWilliam/ActionFPS/tree/master/www" target="_blank">the website source code</a>
        for some examples.</p>
        
        <h2>Query the most recent games</h2>
        <pre><code><a href="http://api.actionfps.com/recent/" rel="nofollow" target="_blank">http://api.actionfps.com/recent/</a></code></pre>

        <h2>Query the most recent clangames</h2>
        <pre><code><a href="http://api.actionfps.com/recent/clangames/" rel="nofollow" target="_blank">http://api.actionfps.com/recent/clangames/</a></code></pre>

        <h2>Query a game by ID</h2>
        <pre><code><a href="http://api.actionfps.com/game/?id=2015-12-05T23:48:55Z" rel="nofollow" target="_blank">http://api.actionfps.com/game/?id=2015-12-05T23:48:55Z</a></code></pre>

        <h2>Query clans</h2>
        <pre><code><a href="http://api.actionfps.com/clans/" rel="nofollow" target="_blank">http://api.actionfps.com/clans/</a></code></pre>

        <h2>Query users</h2>
        <pre><code><a href="http://api.actionfps.com/users/" rel="nofollow" target="_blank">http://api.actionfps.com/users/</a></code></pre>

        <h2>Query a full user profile</h2>
        <pre><code><a href="http://api.actionfps.com/user/lozi/full/" rel="nofollow" target="_blank">http://api.actionfps.com/user/lozi/full/</a></code></pre>

        <h2>Query users with full profile</h2>
        <pre><code><a href="http://api.actionfps.com/users/full/" rel="nofollow" target="_blank">http://api.actionfps.com/users/full/</a></code></pre>

        <h2>Query <span style="text-decoration: underline;">ALL GAMES</span></h2>
        <p>This is a goldmine for data exploration</p>
        <pre><code><a href="http://api.actionfps.com/all/" rel="nofollow" target="_blank">http://api.actionfps.com/all/</a></code></pre>
        
    </article>

<?php echo $foot; ?>
