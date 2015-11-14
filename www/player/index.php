<?php
if (!ctype_alnum($_GET['id'])) die("id invalid");
require_once("../render.inc.php");
require("../render_game.inc.php");

$user = json_decode(file_get_contents("http://api.actionfps.com/user/" . $_GET['id'] . "/full/"), true);
$achievements = $user['achievements'];

function capture_master($maps) {
    ?><section class="content">

    <div class="master">
    <table class="map-master">
    <thead><tr><th>Map</th><th>RVSF</th><th>CLA</th></tr></thead>
    <tbody>
    <?php foreach($maps as $map) {

    ?>
    <tr class="<?php echo $map['completed'] ? "complete" : "incomplete"; ?>">
        <th>ctf @ <?php echo $map['map']; ?></th>
    <td class="cla <?php echo $map['cla'] == '3/3' ? 'complete' : 'partial'; ?>"><?php echo $map['cla']; ?></td>
    <td class="rvsf <?php echo $map['rvsf'] == '3/3' ? 'complete' : 'partial'; ?>"><?php echo $map['rvsf']; ?></td>

</tr>
<?php } ?>
</tbody>
</table>
</div>
</section> <?php
}

function progress_val($percent)
{
    if ($percent <= 50) {
        $rightDeg = round(90 + 3.6 * $percent);
        return "linear-gradient(90deg, #2f3439 50%, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0)), linear-gradient(${rightDeg}deg, #ff6347 50%, #2f3439 50%, #2f3439);";
    } else {
        $leftDeg = round(3.6 * $percent - 270);
        return "linear-gradient(" . $leftDeg . "deg, #ff6347 50%, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0)), linear-gradient(270deg, #ff6347 50%, #2f3439 50%, #2f3439);";
    }
}

function completed_achievement($achievement)
{
    ?>
    <div class="AchievementCard achievement achieved">
        <div class="cont">
            <div class="achievement-left">
                <div class="progress-radial progress-100">
                    <div class="overlay">✔</div>
                </div>
            </div>
            <div class="achievement-description">
                <header>
                    <h3><?php echo $achievement['title']; ?></h3>

                    <p><?php echo $achievement['description']; ?></p>
                </header>
                <?php

                if ( isset($achievement['extra']) && isset($achievement['extra']['maps'])) {
                    capture_master($achievement['extra']['maps']); }
    ?>
                    <p
} class="achieved-on">
                    <time is="relative-time" datetime="<?php echo $achievement['at']; ?>">
                        <?php echo $achievement['at']; ?>                    </time>
                </p>
            </div>
        </div>
    </div>
    <?php
}

function progress_achievement($achievement)
{
    ?>
    <div class="AchievementCard achievement notAchieved">
        <div class="cont">
            <div class="achievement-left">
                <div class="progress-radial" style="background-image: <?php echo progress_val($achievement['percent']); ?>">
                    <div class="overlay"><?php echo $achievement['percent']; ?>%</div>
                </div>
            </div>
            <div class="achievement-description">
                <header>
                    <h3><?php echo $achievement['title']; ?></h3>

                    <p><?php echo $achievement['description']; ?></p>
                </header>
                <?php if ( isset($achievement['extra']) && isset($achievement['extra']['maps'])) {
                    capture_master($achievement['extra']['maps']); }
                ?>
            </div>
        </div>
    </div>
    <?php
}

function none_achievement($achievement)
{
    ?>
    <div class="AchievementCard achievement notAchieved">
        <div class="cont">
            <div class="achievement-left">
                <div class="progress-radial progress-0">
                    <div class="overlay"></div>
                </div>
            </div>
            <div class="achievement-description">
                <header>
                    <h3><?php echo $achievement['title']; ?></h3>

                    <p><?php echo $achievement['description']; ?></p>
                </header>
            </div>
        </div>
    </div>
    <?php
}

?>

    <article id="profile">
    <div class="profile">
        <h1><?php echo htmlspecialchars($user['nickname']['nickname']); ?></h1>
        <div class="main-info">
            <div class="basics">
                <table class="basic-counts">
                    <tr>
                        <th>Time played</th><td><?php
                            $tp = $user['stats']['timePlayed'];
                            $tp = floor($tp / 60);
                            if ( $tp == 0 ) {
                                echo "not enough";
                            } else if ( $tp > 24 ) {
                                $days = floor($tp / 24);
                                $hours = $tp - $days * 24;
                                echo "$days days, $hours hours";
                            } else {
                                echo "$tp hours";
                            }
                            ?></td>
                        <th>Flags</th><td><?php echo $user['stats']['flags']; ?></td>

                    </tr>
                    <tr>
                        <th>Games played</th><td><?php echo $user['stats']['gamesPlayed']; ?></td>
                        <th>Frags</th><td><?php echo $user['stats']['frags']; ?></td>
                    </tr>
                </table>
            </div>
        <div class="achievements">
            <h3>Achievements</h3>

            <div class="achievements">
                <?php foreach ($achievements['completedAchievements'] as $achievement) {
                    completed_achievement($achievement);
                } ?>
                <?php foreach ($achievements['partialAchievements'] as $achievement) {
                    progress_achievement($achievement);
                } ?>
                <?php foreach ($achievements['switchNotAchieveds'] as $achievement) {
                    none_achievement($achievement);
                } ?>
            </div>
        </div>
        </div>
    </div>

    </article><?php
echo $foot;
