<?php
use ActionFPS\EmptyActionReference;

ini_set("display_errors", "Off");
require "vendor/autoload.php";
require "state/Clanwars.php";
require "state/ClanStats.php";
$clanwars = new ClanwarsAccumulator();
$state = $clanwars->initialState();
if (isset($_POST['games'])) {
    $games = json_decode($_POST['games'], false);
    foreach ($games as $game) {
        $state = $clanwars->reduce(new EmptyActionReference(), $state, $game);
    }
} else if (isset($_POST['all-games'])) {
    $lines = explode("\n", $_POST['all-games']);
    foreach ($lines as $line) {
        list(, $game_json) = explode("\t", $line);
        $game = json_decode($game_json, false);
        $state = $clanwars->reduce(new EmptyActionReference(), $state, $game);
    }
} else if ( isset($_POST['clanwars'])) {
    $clanwars = json_decode($_POST['clanwars'], false);
    $clanstats = new ClanStatsAccumulator();
    $state = $clanstats->initialState();
    foreach($clanwars as $clanwar) {
        $state = $clanstats->reduce(new EmptyActionReference(), $state, $clanwar);
    }
}
echo json_encode($state);

