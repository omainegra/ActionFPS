<?php
$servers = json_decode(file_get_contents('http://api.actionfps.com/servers/'), true);

foreach($servers as $server) { ?>
addserver <?php echo $server['hostname'] ?> <?php echo $server['port'] ?>;

<?php } ?>
