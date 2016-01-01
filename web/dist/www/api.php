<?php
require_once("render.inc.php");
require("render_game.inc.php");
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

        <h2>Find a game by ID</h2>
        <pre><code><a href="http://api.actionfps.com/game/?id=2015-12-05T23:48:55Z" rel="nofollow" target="_blank">http://api.actionfps.com/game/?id=2015-12-05T23:48:55Z</a></code></pre>

        <h2>List clans</h2>
        <pre><code><a href="http://api.actionfps.com/clans/" rel="nofollow" target="_blank">http://api.actionfps.com/clans/</a></code></pre>

        <h2>List users</h2>
        <pre><code><a href="http://api.actionfps.com/users/" rel="nofollow" target="_blank">http://api.actionfps.com/users/</a></code></pre>

        <h2>List nicknames</h2>
        <pre><code><a href="http://api.actionfps.com/nicknames/" rel="nofollow" target="_blank">http://api.actionfps.com/nicknames/</a></code></pre>

        <h2>List nicknames with game counts</h2>
        <pre><code><a href="http://api.actionfps.com/nicknames/?with=game-counts" rel="nofollow" target="_blank">http://api.actionfps.com/nicknames/?with=game-counts</a></code></pre>

        <h2>Query a full user profile</h2>
        <pre><code><a href="http://api.actionfps.com/user/lozi/full/" rel="nofollow" target="_blank">http://api.actionfps.com/user/lozi/full/</a></code></pre>

        <h2>Query user by e-mail</h2>
        <p>You would use this if you are building a custom service and want to get the user's ID based on their GMail address.</p>
        <pre><code><a href="http://api.actionfps.com/user/drakas.tralas@googlemail.com/" rel="nofollow" target="_blank">http://api.actionfps.com/user/drakas.tralas@googlemail.com/</a></code></pre>

        <h2>Query users with full profile</h2>
        <pre><code><a href="http://api.actionfps.com/users/full/" rel="nofollow" target="_blank">http://api.actionfps.com/users/full/</a></code></pre>

        <h2>Query <span style="text-decoration: underline;">ALL GAMES</span></h2>
        <p>This is a goldmine for data exploration</p>
        <pre><code><a href="http://api.actionfps.com/all/" rel="nofollow" target="_blank">http://api.actionfps.com/all/</a></code></pre>
        
    </article>

<?php echo $foot; ?>
