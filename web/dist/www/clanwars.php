<?php
require_once "render.inc.php";
require "render_game.inc.php";


$clanwars = json_decode($_POST['clanwars'], true);
?>
<div id="games">
    <br /><br />
    <?php foreach($clanwars as $war) render_war($war); ?>
</div>
<?php echo $foot;
