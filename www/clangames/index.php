<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$url = "http://api.actionfps.com/recent/clangames/";
$games = json_decode(file_get_contents($url), true);
?><br/>
<br/>
    <div id="games"><div id="existing-games"><?php
            foreach($games as $game) {
                render_game($game);
            }

            ?></div></div>
<?php echo $foot;