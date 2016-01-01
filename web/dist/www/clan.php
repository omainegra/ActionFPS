<?php
require_once("render.inc.php");
require("render_game.inc.php");

$clan = json_decode($_POST['clan'], true);
$stats = $clan['stats'];
?>
<article id="profile">
    <div class="profile">
            <div class="clan-header">
                <?php clan_logo($clan); ?>
                <h1><?php echo htmlspecialchars($clan['fullName']) ?></h1>
                <?php if(isset($clan['website'])) { ?>
                  <div>
                    - <a href="<?php echo htmlspecialchars($clan['website']) ?>" target="_blank">Website</a>
                  </div>
                <?php } ?>
            </div>
            <div class="main-info">
                <div class="basics">
                    <table class="basic-counts">
                        <tr>
                            <th>Clanwar wins</th><td><?php echo $stats['wins'] ?>/<?php echo $stats['wars'] ?></td>
                            <th>Game wins</th><td><?php echo $stats['gamewins'] ?>/<?php echo $stats['games'] ?></td>

                        </tr>
                        <tr>
                            <th>Flags</th><td><?php echo $stats['flags'] ?></td>
                            <th>Frags</th><td><?php echo $stats['frags'] ?></td>
                        </tr>
                        <?php if($stats['rank']) { ?>
                        <tr>
                            <th>Elo Rank</th><td><?php echo $stats['rank'] ?></td>
                            <th>Elo Points</th><td><?php echo round($stats['elo']) ?></td>
                        </tr>
                        <?php } else { ?>
                        <tr>
                            <th>Elo Rank</th><td>Play more to get ranked</td>
                        </tr>
                        <?php } ?>
                    </table>
                </div>
           </div>
    </div>
    <div id="games">
        <h3>Recent Wars</h3>
        <ol class="recent-games">
        <?php foreach($clan['wars'] as $war) : ?>
            <li><?php render_compact_war($war, $clan['id']) ?></li>
        <?php endforeach; ?>
        </ol>
    </div>
</article>
       

<?php echo $foot;