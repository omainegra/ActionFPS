<?php
if (!ctype_alnum($_GET['id'])) die("id invalid");
require_once("../render.inc.php");
require("../render_game.inc.php");

$achievements = json_decode(file_get_contents("http://api.actionfps.com/achievements/" . $_GET['id'] . "/"), true);

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

    </article><?php
echo $foot;
