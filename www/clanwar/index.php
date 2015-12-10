<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$html = file_get_contents("http://woop.ac:81/html/clanwar/?id=" . $_GET['id']);
?>  <div id="game"><?php echo $html ?></div><?php
echo $foot;