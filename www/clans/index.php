<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$clans = json_decode(file_get_contents('http://api.actionfps.com/clans/'), true);
?>
<article id="players">
   <ol>
       <?php foreach($clans as $clan) { ?>
            <li>
                <a href="/clan/?id=<?php echo htmlspecialchars($clan['id']) ?>" title="<?php echo htmlspecialchars($clan['fullName']) ?>">
                    <img src="http://woop.ac:81/html/clan_picture.php?name=<?php echo htmlspecialchars($clan['name']) ?>&id=<?php echo htmlspecialchars($clan['id']) ?>" width="64">
                </a>
            </li>
       <?php } ?>
   <ol>
</article>

<?php echo $foot;