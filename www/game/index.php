<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$game = json_decode($_POST['game'] ?: file_get_contents("http://api.actionfps.com/game/?id=" . rawurlencode((string)$_GET['id'])), true);
if ($game) {
    ?>
    <div id="game"><?php
    render_game($game); ?></div><?php
}
echo $foot;