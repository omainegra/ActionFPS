<?php
ini_set("display_errors", "Off");
require "render_game.inc.php";
$game = json_decode(file_get_contents("php://input"), true);
if ( isset($game['map']) ) {
    render_game($game);
}