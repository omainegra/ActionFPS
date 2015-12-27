<?php
require_once("render.inc.php");
require("render_game.inc.php");

$games = source_data("recent");
$events = source_data("events");
$latest_clanwar = source_data("clanwar");
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

    <?php if ( $latest_clanwar ) {  ?> <div id="latest-clanwar"><?php render_war($latest_clanwar, false, true); ?></div><?php } ?>


    <div id="existing-games"><?php
foreach($games as $game) {
    render_game($game);
}

?></div></div>

<?php require("live/bits.inc.php"); ?>
<?php

echo $foot;