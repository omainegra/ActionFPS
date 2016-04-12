<?php

function clan_logo($clan) {
    $url = @$clan['logo'] ?: 'http://woop.ac:81/html/clan_picture.php?name='.rawurlencode($clan['name']).'&id='.rawurlencode(@($clan['id'] ?: $clan['clan']));
    ?><img class="clan-logo" src="<?php echo htmlspecialchars($url); ?>" width="64" height="64"/>
<?php

}
$main_template_path = dirname(__FILE__) . "/template.html";
$domdoc = new DOMDocument();
libxml_use_internal_errors(true);
$domdoc->loadHTMLFile($main_template_path);
libxml_use_internal_errors(false);
if (isset($title)) {
    $domdoc->getElementsByTagName("title")->item(0)->textContent = $title;
}
$split_text = "---SPLIT HERE---";
$content_node = $domdoc->getElementById("content");
$split_text_node = $domdoc->createTextNode($split_text);
$content_node->replaceChild($split_text_node, $content_node->childNodes->item(0));
$log_in = $domdoc->getElementById('log-in');
if (@($_GET['supports'] == 'json')){
    $domdoc->getElementById("content")->setAttribute("data-has-json", "has-json");
}

if ( isset($_GET['af_name']) ) $_COOKIE['af_name'] = $_GET['af_name'];
if ( isset($_GET['af_id']) ) $_COOKIE['af_id'] = $_GET['af_id'];
if ( isset($_COOKIE['af_name'], $_COOKIE['af_id'])) {
    $player_name = $domdoc->createTextNode($_COOKIE['af_name']);
    $log_in->replaceChild($player_name, $log_in->childNodes->item(0));
    $log_in->setAttribute("href", "/player/?id=".rawurlencode($_COOKIE['af_id']));
    // remove log in link, it's getting on my nerves now with accidental clicking :D
//    $dac = $domdoc->getElementById("download-ac-button");
//    $dac->parentNode->removeChild($dac);
    $rp = $domdoc->getElementById("reg-menu-reg-play")->parentNode;
    $rp->parentNode->removeChild($rp);
} else {
    $rp = $domdoc->getElementById("reg-menu-play")->parentNode;
    $rp->parentNode->removeChild($rp);
}
list($head, $foot) = explode($split_text, $domdoc->saveHTML());
if (!isset($skip_head) || $skip_head === false) {
    echo $head;
}
