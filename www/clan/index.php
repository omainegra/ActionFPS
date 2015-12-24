<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$clan = json_decode(file_get_contents('http://woop.ac:81/ActionFPS-PHP-Iterator/api/clan.php?id=' . $_GET['id']));
$stats = $clan->stats;
?>
<article id="profile">
    <div class="profile">
            <div class="clan-header" style="display: flex; align-items: center; margin-bottom: 1em">
                <img style="margin-right: 0.25em;" src="http://woop.ac:81/html/clan_picture.php?name=<?php echo htmlspecialchars($clan->name) ?>&id=<?php echo htmlspecialchars($clan->id) ?>" width="64">
                <h1 style="margin: 0 0.25em"><?php echo htmlspecialchars($clan->fullName) ?></h1>
                <?php if(!empty($clan->website)) : ?>
                  <div>
                    - <a href="<?php echo htmlspecialchars($clan->website) ?>" target="_blank">Website</a>
                  </div>
                <?php endif; ?>
            </div>
            <div class="main-info">
                <div class="basics">
                    <table class="basic-counts">
                        <tr>
                            <th>Clanwar wins</th><td><?php echo $stats->wins ?>/<?php echo $stats->wars ?></td>
                            <th>Game wins</th><td><?php echo $stats->gamewins ?>/<?php echo $stats->games ?></td>

                        </tr>
                        <tr>
                            <th>Flags</th><td><?php echo $stats->flags ?></td>
                            <th>Frags</th><td><?php echo $stats->frags ?></td>
                        </tr>
                        <?php if($stats->rank) : ?>
                        <tr>
                            <th>Elo Rank</th><td><?php echo $stats->rank ?></td>
                            <th>Elo Points</th><td><?php echo round($stats->elo) ?></td>
                        </tr>
                        <?php else : ?>
                        <tr>
                            <th>Elo Rank</th><td>Play more to get ranked</td>
                        </tr>
                        <?php endif; ?>
                    </table>
                </div>
           </div>
    </div>
    <div id="games">
        <h3>Recent Wars</h3>
        <ol style="list-style-type: none;">
        <?php foreach($clan->wars as $war) : ?>
            <li style="line-height: 1.5em;"><?php render_compact_war($war, $clan->id) ?></li>
        <?php endforeach; ?>
        </ol>
    </div>
</article>
       

<?php echo $foot;