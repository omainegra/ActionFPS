<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$url = "http://api.actionfps.com/clans/json/";
$yaml = str_replace("json", "yaml", $url);
$clans = json_decode(file_get_contents($url), true);
?>
    <article id="questions">
        <?php foreach ($clans as $id => $clan) {
            ?>

            <h3><?php echo htmlspecialchars($clan['full name']); ?></h3>
            <?php if (isset($clan['website'])) { ?>
            <p><a
            href="<?php echo htmlspecialchars($clan['website']); ?>"
            target="_blank">Website</a></p><?php }
        } ?>
        <hr/>
        <p>This list is available as <a href="<?php echo $url; ?>">JSON</a> and
            <a href="<?php echo $yaml; ?>">YAML</a>
        </p>
    </article>
<?php echo $foot;