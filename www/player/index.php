<?php
if (!ctype_alnum($_GET['id'])) die("id invalid");
require_once("../render.inc.php");
require("../render_game.inc.php");

$achievements = json_decode(file_get_contents("http://api.actionfps.com/achievements/" . $_GET['id'] . "/"), true);

function completed_achievement($achievement) {
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
                <p class="achieved-on">
                    <time is="relative-time" datetime="<?php echo $achievement['at']; ?>">
                        <?php echo $achievement['at']; ?>                    </time>
                    </p>
            </div>
        </div>
    </div>
    <?php
}
function progress_achievement($achievement) {
    ?>
    <div class="AchievementCard achievement notAchieved">
        <div class="cont">
            <div class="achievement-left">
                <div class="progress-radial progress-<?php echo $achievement['percent']; ?>">
                    <div class="overlay"><?php echo $achievement['percent']; ?>%</div>
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
function none_achievement($achievement) {
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
 <?php foreach($achievements['completedAchievements'] as $achievement) {
    completed_achievement($achievement);
} ?>
 <?php foreach($achievements['partialAchievements'] as $achievement) {
     progress_achievement($achievement);
} ?>
 <?php foreach($achievements['switchNotAchieveds'] as $achievement) {
     none_achievement($achievement);
} ?>
                </div>
            </div>
        </div>

    </article><?php
echo $foot;
