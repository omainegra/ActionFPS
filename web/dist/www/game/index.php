<?php
$has_json = true;
require_once("../render.inc.php");
require("../render_game.inc.php");

$game = source_data();
if ($game) {
    ?>
    <div id="game"><?php
    render_game($game); ?></div><?php
}
echo $foot;