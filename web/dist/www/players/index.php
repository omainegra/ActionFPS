<?php
$has_json = true;

require_once("../render.inc.php");
require("../render_game.inc.php");
$users = source_data();
usort($users, function($a, $b) {
    return strcmp(strtolower($a["name"]), strtolower($b["name"]));
});

?>
<article id="players"><ol><?php
foreach($users as $user) {
    ?><li><a href="/player/?id=<?php echo $user['id']; ?>"><?php echo htmlspecialchars($user['name']); ?></a></li><?php
}
?></ol></article><?php
echo $foot;
