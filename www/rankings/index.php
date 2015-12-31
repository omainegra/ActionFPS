<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$clans = json_decode($_POST['rankings'] ?: file_get_contents('http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanstats.php?count=10'), true)['now'];
?>
<article id="questions">
    <div id="rank">
    <h2>Clan Ranks</h2>
    <table style="width: 480px;">
        <tr>
            <th>Clan</th>
            <td>Wars</td>
            <td>Won</td>
            <td>Games</td>
            <td>Score</td>
            <td>Elo Rank</td>
        </tr>
        <?php foreach($clans as $clan) : ?>
        <tr>
            <th><a href="/clan/?id=<?php echo htmlspecialchars($clan['clan']) ?>"><?php echo htmlspecialchars($clan['name']) ?></a></th>
            <td><?php echo $clan['wars'] ?></td>
            <td><?php echo $clan['wins'] ?></td>
            <td><?php echo $clan['games'] ?></td>
            <td><?php echo $clan['score'] ?></td>
            <td><?php echo $clan['rank'] ?></td>
        </tr>
        <?php endforeach; ?>
    </table>
    </div>
</article>


<?php echo $foot;