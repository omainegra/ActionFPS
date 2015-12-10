<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$html = file_get_contents("http://woop.ac:81/html/clan/?id=" . $_GET['id']);
?>
    <article id="profile">
        <?php echo $html ?>
    </article>
<?php echo $foot;