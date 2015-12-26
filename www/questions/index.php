<?php
require_once("../render.inc.php");
require("../render_game.inc.php");
?>
<article id="questions">
    <h2 id="what-is">What is this?</h2>
    <p>This is a clan match recording system for <a href="http://woop.us/" target="_blank">Woop Clan's</a> <a href="http://assault.cubers.net/" target="_blank">AssaultCube</a> servers.</p>
    <h2>Is this project open source?</h2>
    <p>Yes, see GitHub: <a href="http://github.com/ScalaWilliam/actionfps" target="_blank">ScalaWilliam/actionfps</a>. Contributions, issues, pull requests are welcome.</p>
    <h2>History of the project</h2>
    <p><strong>January 2014</strong> Idea for woop.ac born, started work, ended up working on duel.gg.</p>
    <p><strong>December 2014</strong> <a href="http://woop.us/woop.ac" target="_blank">woop.ac released</a>.</p>
    <p><strong>January 2015</strong> <a href="http://woop.us/2199" target="_blank">woop.ac</a> Achievements
        <a href="http://woop.us/2198" target="_blank">released</a>
        .
    </p>
    <p><strong>February 2015</strong> Idea for actionfps born.</p>
    <p><strong>October 2015</strong> <a href="https://github.com/ScalaWilliam/woop.ac" target="_blank">woop.ac open sourced</a>.</p>
    <p><strong>November 2015</strong> woop.ac turned into <a href="https://github.com/ScalaWilliam/actionfps" target="_blank">actionfps and open sourced</a>.</p>
    <h2>What's the magic behind actionfps?</h2>
    <p>The genius of <a target="_blank" href="https://www.scalawilliam.com/">William Narmontas, professional software engineer</a>.<br/>
        Technology-wise, it's Play, Scala, Akka and PHP.</p>
    <h2>AssaultCube</h2>
    <iframe width="560" height="315" src="//www.youtube-nocookie.com/embed/1k5sI1Rz558?rel=0" frameborder="0" allowfullscreen></iframe>
    <h2>I'd like to talk to you</h2>
    <p>Find us on <a href="http://www.teamspeak.com/?page=downloads">TeamSpeak</a> at our server <a href="ts3server://aura.woop.ac:9988" target="_blank">&quot;aura.actionfps&quot;</a>.</p>
    <p>Also join us on <a href="https://webchat.gamesurge.net/?channels=woop-clan">#woop-clan @ GameSurge</a> IRC channel.</p>
    <h2>Which servers do you record from?</h2>
    <p>See the <a href="/servers/">servers</a> page.</p>
    <h2>Any rules for play?</h2>
    <p>Be fair.</p>
    <h2>What are your plans?</h2>
    <p>Expand. We want your participation.</p>
    <h2 id="what-is-the-matchclient"><a href="#what-is-the-matchclient">What is the MatchClient?</a></h2>
    <p>The MatchClient is a custom client we made to better suits the needs of clanwars. 
    With it you will be able to access compatible servers that support features like <strong>/pause</strong>!</p>
    <h2>Why should I <a href="/login/">register</a>?</h2>
    <p>When registering, it creates your profile page where you can find statistics about all your games on our servers. 
    You will also unlock achievements while playing!</p>
    <h2>Why Google sign in?</h2>
    <p>Saves us time in development.</p>
    <h2 id="can-i-change-name"><a href="#can-i-change-name">Can I change the in-game name i picked while registering?</a></h2>
    <p>In the current state you are not able to change it. 
        However if the name change is necessary (i.e joined or left a clan..) then you can make a post on the <a href="https://groups.google.com/forum/#!forum/actionfps" target="_blank">forum</a> and we will do it for you. </p>
    <h2>Why are some games circled with blue on the frontpage?</h2>
    <p>This means that the games are happenning right now ! The real time game display allows you to see the score of a game in real time on your browser!</p>
    <h2 id="should-i-accept-notifications"><a href="#should-i-accept-notifications">Should I accept notifications?</a></h2>
    <p>Yes! When you are registered on the website, you are able to use an inter finding feature we created. Go on any of our <a href="/servers/">servers</a>, then type <strong>!inter.</strong>
        This will send a notification on people's browser with a link to join the inter. There is a spam protection, doing it once is enough.</p>
    <h2 id="completed-clanwar"><a href="#completed-clanwar">How to complete a Clan War ?</a></h2>
    <p>In order to complete a Clan War, you need to play at least 2 consecutive games against the same clan. Moreover, if these games resulted in 2 ties or 1 win for both clans, a third game has to be played as a tie breaker. </p>
    <h2 id="how-can-i-support"><a href="#how-can-i-support">How can I support the project?</a></h2>
    <p>By registering and playing ! You can also post your ideas and feedback on the forum. Don't hesitate to report any <a href="https://github.com/ScalaWilliam/ActionFPS/issues" target="_blank">issue</a> you have.
        If you are willing to be actively participating in the development then join on on Gitter! </p>
</article>

<?php echo $foot; ?>
