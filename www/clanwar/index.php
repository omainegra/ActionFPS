<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$clanwar = json_decode(file_get_contents('http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwar.php?id=' . $_GET['id']));
?>
<div id="game">
<?php render_war($clanwar, true); ?>

<?php foreach($clanwar->games as $game) : ?>
    <?php render_game(json_decode(file_get_contents('http://api.actionfps.com/game/?id=' . $game->startTime . 'Z'), true)); ?>
<?php endforeach; ?>
</div><?php
echo $foot;