<?php
require_once "render.inc.php";

$servers = json_decode($_POST['servers'], true);
$groups = array_unique(array_map(function ($server) {
    return $server['region'];
}, $servers));
?>
    <article id="servers">
        <h2>AssaultCube</h2>

        <?php foreach ($groups as $group) {
            ?><h3><?php echo htmlspecialchars($group); ?></h3>

            <ul>
                <?php foreach ($servers as $server) {
                    if ($server['region'] == $group) {

                        $url = "assaultcube://" . $server['hostname'] . ":" . $server['port'] . (isset($server['password']) ? '?password=' . rawurlencode($server['password']) : '');
                        ?>
                        <li><a href="<?php echo $url; ?>">
                                <?php echo $server['hostname'] . ' ' . $server['port']; ?>
                            </a>
                            <?php if ($server['kind'] != "Standard") { ?> (<?php echo htmlspecialchars($server['kind']); ?>) <?php } ?>
                        </li>
                    <?php }
                } ?>
            </ul>

        <?php } ?>
    </article>

<?php echo $foot; ?>