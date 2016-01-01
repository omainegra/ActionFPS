<?php
$input = file_get_contents('php://input');
$game = json_decode($input, true);
require_once("../render_game.inc.php");
if ( isset($game['map']) ) {
    render_game($game);
}


