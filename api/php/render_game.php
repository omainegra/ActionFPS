<?php
ini_set("display_errors", "Off");
require "render_game.inc.php";
render_game(json_decode(file_get_contents("php://input"), true));