<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$clanwars = source_data();
?>
<div id="games">
    <br /><br />
    <?php foreach($clanwars as $war) render_war($war); ?>
</div>
<?php echo $foot;
