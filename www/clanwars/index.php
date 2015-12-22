<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$html = file_get_contents("http://woop.ac:81/html/clanwars/");
?>
<?php echo $html ?>
<?php echo $foot;