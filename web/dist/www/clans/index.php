<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$clans = source_data();
?>
<article id="clans">
   <ol>
       <?php foreach($clans as $clan) { ?>
            <li>
                <a href="/clan/?id=<?php echo rawurlencode($clan['id']) ?>" title="<?php echo htmlspecialchars($clan['fullName']) ?>">
                    <?php clan_logo($clan); ?>
                </a>
            </li>
       <?php } ?>
   <ol>
</article>

<?php echo $foot;
