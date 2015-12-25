<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$clans = json_decode(file_get_contents('http://api.actionfps.com/clans/'), true);
?>
<article id="clans">
   <ol>
       <?php foreach($clans as $clan) { ?>
            <li>
                <a href="/clan/?id=<?php echo rawurlencode($clan['id']) ?>" title="<?php echo htmlspecialchars($clan['fullName']) ?>">
                    <?php
                    $logo = @$clan['logo'] ?: 'http://woop.ac:81/html/clan_picture.php?name='.rawurlencode($clan['name']).'&id='.rawurlencode($clan['id']);
                    ?>
                    <img class="clan-logo" src="<?php echo htmlspecialchars($logo); ?>">
                </a>
            </li>
       <?php } ?>
   <ol>
</article>

<?php echo $foot;
