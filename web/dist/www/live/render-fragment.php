<?php
$game = json_decode($_POST['game'], true);
$maps = json_decode($_POST['maps'], true);
require_once("../render_game.inc.php");
if ( isset($game['map']) ) {
    render_game($game, $maps);
}


