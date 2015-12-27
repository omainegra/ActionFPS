<?php
$stuff = file_get_contents("php://input");
$arr = explode("\n", $stuff);
$state = json_decode(array_shift($arr), false);

function iterate($state, $game) {
    if ( !$state ) {
        $state = new stdClass();
        $state->seen = [];
    }
    if ( in_array($game->id, $state->seen)) {
        return $state;
    }
    $state->seen[] = $game->id;
    return $state;
}
foreach($arr as $game_json) {
    $game = json_decode($game_json, false);
    $state = iterate($state, $game);
}
echo json_encode($state, false);
