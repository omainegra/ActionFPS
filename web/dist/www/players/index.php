<?php
require_once("../render.inc.php");
require("../render_game.inc.php");
$users = json_decode($_POST['players'] ?: file_get_contents("http://api.actionfps.com/users/"), true);
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
