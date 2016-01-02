<?php
require_once("render.inc.php");

$players = json_decode($_POST['ranks'], true)['players'];
usort($players, function($a, $b) {
    return $a['rank'] <=> $b['rank'];
});
$players = array_slice($players, 0, 40);
?>
    <article id="questions">
        <div id="rank">
            <h2>Player Ranks</h2>
            <table style="width: 480px;">
                <tr>
                    <td>Rank</td>
                    <td>Player</td>
                    <td>Games</td>
                    <td>Won</td>
                    <td>Elo</td>
                    <td>Score</td>
                    <td>Last played</td>
                </tr>
                <?php foreach($players as $player) : ?>
                    <tr>
                        <td><?php echo $player['rank'] ?></td>
                        <td><a href="/player/?id=<?php echo rawurlencode($player['user']) ?>"><?php echo htmlspecialchars($player['name']) ?></a></td>
                        <td><?php echo $player['games'] ?></td>
                        <td><?php echo $player['wins'] ?></td>
                        <td><?php echo round($player['elo']) ?></td>
                        <td><?php echo $player['score'] ?></td>
                        <td><a href="/game/?id=<?php echo rawurlencode($player['lastGame']); ?>"><time is="relative-time" datetime="<?php echo htmlspecialchars($player['lastGame']); ?>"><?php echo htmlspecialchars($player['lastGame']); ?></time></a>
                        </td>
                    </tr>
                <?php endforeach; ?>
            </table>
        </div>
    </article>


<?php echo $foot;