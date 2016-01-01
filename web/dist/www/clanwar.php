<?php
require_once "render.inc.php";
require "render_game.inc.php";
$clanwar = json_decode($_POST['clanwar'], true);
?>
<div id="game">
<?php render_war($clanwar, true); ?>

<?php foreach($clanwar['games'] as $game) { ?>
    <?php render_game($game); ?>
<?php } ?>
</div><?php
echo $foot;