<?php
require_once("render.inc.php");
require("render_game.inc.php");

$games = json_decode(file_get_contents("http://api.actionfps.com/recent/"), true);
$events = json_decode(file_get_contents("http://api.actionfps.com/events/"), true);
$clanwars = json_decode(file_get_contents('http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?completed=1&count=1'), true);
$latest_clanwar = reset($clanwars);
?><div id="live-events">
    <ol class="LiveEvents live-events">
        <?php foreach($events as $i => $event) {
            if ( $i >= 7 ) { continue; }
        ?>
        <li><a href="/player/?id=<?php echo $event['user']; ?>"><?php echo htmlspecialchars($event['text']); ?></a>
        <span> </span>
            <span class="when">

                <time is="relative-time" datetime="<?php echo $event['date']; ?>">
                    <?php echo $event['date']; ?>                    </time>
            </span>
        </li>
    <?php } ?>

    </ol>

</div><div id="games">

    <div id="dynamic-games">

    </div>

    <div id="new-games"></div>
    
    <div id="latest-clanwar"><?php render_war($latest_clanwar, false, true); ?></div>


    <div id="existing-games"><?php
foreach($games as $game) {
    render_game($game);
}

?></div></div>

<?php require("live/bits.inc.php"); ?>
<?php

echo $foot;