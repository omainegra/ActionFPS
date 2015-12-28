<?php
$has_json = true;

require_once("../render.inc.php");
require("../render_game.inc.php");

$clanwar = source_data();
?>
<div id="game">
<?php render_war($clanwar, true); ?>

<?php foreach($clanwar['games'] as $game) { ?>
    <?php render_game($game); ?>
<?php } ?>
</div><?php
echo $foot;