<?php
function render_game_team_player($game, $team, $player)
{
    ?>
    <li>
        <?php if (isset($player['flags'])) { ?>
            <span class="score flags"><?php echo $player['flags']; ?></span>
        <?php } ?>
        <span class="subscore frags"><?php echo $player['frags']; ?></span>
        <?php if (isset($player['user'])) { ?>
            <span class="name"><a
                    href="/player/?id=<?php echo rawurlencode($player['user']); ?>"><?php echo htmlspecialchars($player['name']); ?></a></span>
        <?php } else { ?>
            <span class="name"><?php echo htmlspecialchars($player['name']); ?></span>
        <?php } ?>
    </li>
    <?php
}

function render_game_team($game, $team)
{
    ?>
<div class="<?php echo htmlspecialchars($team['name']); ?> team">
    <div class="team-header">
        <h3><img src="http://woop.ac/assets/<?php echo htmlspecialchars(strtolower($team['name'])); ?>.png"/></h3>

        <div class="result">
            <?php if (isset($team['flags'])) { ?>
                <span class="score"><?php echo htmlspecialchars($team['flags']); ?></span>
            <?php } ?>
            <span class="subscore"><?php echo htmlspecialchars($team['frags']); ?></span>
        </div>
    </div>
    <div class="players">
        <ol><?php foreach ($team['players'] as $player) {
                render_game_team_player($game, $team, $player);
            } ?>
        </ol>
    </div>
    </div><?php

}

function render_game($game)
{
    if (isset($game['now'])) {
        $ac_link = "assaultcube://" . $game['now']['server']['server'];


    }


    ?>
    <article
        class="GameCard game <?php if (isset($game['now'])) {
            echo "isLive";
        } ?>  <?php if (isset($game['isNew']) && $game['isNew']) {
            echo "isNew";
        } ?> "
        style="background-image: url('http://woop.ac/assets/maps/<?php echo htmlspecialchars($game['map']); ?>.jpg');">
        <div class="w">
            <header>
                <h2>
                    <a
                        <?php if (isset($game['id'])) { ?>
                            href="/game/?id=<?php echo htmlspecialchars($game['id']); ?>"
                        <?php } ?>
                    >
                        <?php echo htmlspecialchars($game['mode']); ?>
                        @
                        <?php echo htmlspecialchars($game['map']); ?>
                        <?php if (!isset($game['now'])) { ?>
                            <time is="relative-time" datetime="<?php echo htmlspecialchars($game['endTime']); ?>">
                                <?php echo htmlspecialchars($game['endTime']); ?>
                            </time>
                        <?php } ?>
                    </a>

                    <?php if (isset($ac_link)) { ?>
                        <a class="server-link"
                           href=<?php echo $ac_link; ?>>on <?php echo $game['now']['server']['shortName']; ?>
                        </a>
                    <?php } ?>


                    <?php if (isset($game['minRemain'])) {
                        ?>
                        <p class="time-remain">
                            <?php
                            if ($game['minRemain'] === 1) {
                                echo '1 minute remains';
                            } else if ($game['minRemain'] === 0) {
                                echo 'game finished';
                            } else {
                                echo htmlspecialchars($game['minRemain']) . " minutes remain";
                            }
                            ?>
                        </p>
                        <?php
                    } ?>
                </h2>
            </header>
            <div class="teams">
                <?php
                foreach ($game['teams'] as $team) {
                    if (strtolower($team['name']) == 'spectator') continue;
                    render_game_team($game, $team);
                }
                ?>
            </div>
            <?php if (isset($game['players']) && is_array($game['players']) && !empty($game['players'])) { ?>
                <div class="dm-players">
                    <ul>
                        <?php foreach ($game['players'] as $player) { ?>
                            <li><span><?php echo htmlspecialchars($player); ?></span></li>
                        <?php } ?>
                    </ul>
                </div>
            <?php } ?>
            <?php if (isset($game['spectators']) && !empty($game['spectators'])) { ?>
                <div class="spectators">
                    <h4>Spectators:</h4>
                    <ul>
                        <?php foreach ($game['spectators'] as $spectator) {
                            ?>
                            <li><span><?php echo htmlspecialchars($spectator); ?></span></li>
                        <?php } ?>
                    </ul>
                </div>

            <?php } ?>
        </div>

    </article>
<?php } ?>