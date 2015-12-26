<?php
ini_set("display_errors", "Off");
require "vendor/autoload.php";
require "state/Clanwars.php";
require "state/ClanStats.php";
$state = null;
if (isset($_POST['games'])) {
    $games = json_decode($_POST['games'], false);
    foreach ($games as $game) {
        $state = games_to_clanwars($state, $game);
    }
} else if ( isset($_POST['clanwars'])) {
    $clanwars = json_decode($_POST['clanwars'], false);
    foreach($clanwars as $clanwar) {
        $state = clanwars_to_clanstats($state, $clanwar);
    }
}
echo json_encode($state);

