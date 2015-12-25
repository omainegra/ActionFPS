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
            <span class="name"><span><?php echo htmlspecialchars($player['name']); ?></span></span>
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
    if ( !isset($game['now']) && isset($game['server']) && strpos($game['server'], 'aura') > -1 ) {
        $demo_link = 'http://woop.ac:81/find-demo.php?time=' . rawurlencode($game['id']) . '&map=' . rawurlencode($game['map']);
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

                    <?php if (isset($demo_link)) { ?>
    <a target="_blank" class="demo-link" href="<?php echo htmlspecialchars($demo_link) ?>">demo</a>
    <?php } ?>

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

<?php
function render_war_clan_player($war, $clan, $player)
{
    ?>
    <li>
        <?php if (isset($player['flags'])) { ?>
            <span class="score flags"><?php echo $player['flags']; ?></span>
        <?php } ?>
        <span class="subscore frags"><?php echo $player['frags']; ?></span>
        <span class="name">
            <?php if (isset($player['user'])) { ?>
                <a href="/player/?id=<?php echo rawurlencode($player['user']); ?>"><?php echo htmlspecialchars($player['name']); ?></a>
            <?php } else { ?>
                <?php echo htmlspecialchars($player['name']); ?>
            <?php } ?>
            <?php if(isset($clan['trophies']['mvp']) && $clan['trophies']['mvp']['name'] == $player['name']) { ?>
                <img src="http://woop.ac:81/html/assets/mvp.png" title="MVP"  />
            <?php } ?>
        </span>
    </li>
    <?php
}?>

<?php
function render_war_clan($war, $clan, $show_players = false)
{
?>
    <div class="team">
        <div class="team-header">
            <h3><a href="/clan/?id=<?php echo rawurlencode($clan['clan']) ?>"><?php clan_logo($clan); ?></a></h3>

            <div class="result">
                <span class="clan"><a href="/clan/?id=<?php echo rawurlencode($clan['clan']) ?>"><?php echo htmlspecialchars($clan['name']) ?></a></span>
                <span class="score"><?php echo $clan['wins']; ?></span>
            </div>
        </div>
        <?php if($show_players) : ?>
       <div class="players">
           <ol><?php foreach ($clan['players'] as $player) {
                render_war_clan_player($war, $clan, $player);
            } ?>
           </ol>
        </div>
        <?php endif; ?>
    </div>
<?php } ?>
<?php 
function render_war($war, $show_players = false, $show_as_first = false)
{
    $finalgame = $war['games'][count($war['games'])-1];
    ?>
    <article class="GameCard game clanwar" style="background-image: url('http://woop.ac:81/html/assets/clanwar.png')">
        <div class="w">
            <header>
                <h2>
                    <a href="/clanwar/?id=<?php echo rawurlencode($war['startTime']); ?>"
                    >
                        <time is="local-time" datetime="<?php echo $war['endTime']; ?>" weekday="short" year="numeric" month="short" day="numeric">
                            <?php echo $war['endTime']; ?>
                        </time>


                    </a>
                    <?php if ( $show_as_first ) {?><a class="lcw" title="Latest clanwar"><i class="fa fa-question-circle"></i></a><?php } ?>


                </h2>
             </header>
             
            <div class="teams">
                <?php foreach($war['clans'] as $clan) { ?>
                    <?php render_war_clan($war, $clan, $show_players); ?>
                <?php } ?>
             </div>
          </div>

    </article>
<?php } ?>

<?php 
function render_compact_war($war, $perspective)
{
    if($war['clans'][0]['clan'] == $perspective)
    {
        $clan = $war['clans'][0];
        $opponent = $war['clans'][1];
    }
    else
    {
        $clan = $war['clans'][1];
        $opponent = $war['clans'][0];
    }
?>
<?php if(!isset($war['winner'])) { ?>
        Tied <?php echo $clan['wins'] ?> - <?php echo $opponent['wins'] ?> vs
<?php } elseif($war['winner'] == $clan['clan']) { ?>
        Won <?php echo $clan['wins'] ?> - <?php echo $opponent['wins'] ?> vs
<?php } else { ?>
        Lost <?php echo $clan['wins'] ?> - <?php echo $opponent['wins'] ?> vs
<?php } ?>
        <a href="/clan/?id=<?php echo rawurlencode($opponent['clan']) ?>"><?php echo htmlspecialchars($opponent['name']) ?></a>
        -
        <a href="/clanwar/?id=<?php echo rawurlencode($war['startTime']); ?>">
             <time is="local-time" datetime="<?php echo htmlspecialchars($war['endTime']); ?>" weekday="short" year="numeric" month="short" day="numeric">
                 <?php echo htmlspecialchars($war['endTime']); ?>
             </time> (<?php echo $war['teamsize'] ?> vs <?php echo $war['teamsize'] ?>)
         </a>
        
<?php } ?>
