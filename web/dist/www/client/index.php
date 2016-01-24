<?php
require("../render.inc.php");
?>
<article id="questions">
<h1>Download</h1>
<div class="download-buttons">
  <a href="http://woop.ac:81/windows_client_1202.2.exe" class="download-button">
    <div class="os-icon"><img src="https://cloud.githubusercontent.com/assets/5359646/12221435/01770336-b79b-11e5-98b0-f7f220fb1f79.png"></div>
    <div class="download-content">
      <div>Match Client</div>
      <span class="version">1202.2</span>
    </div>
  </a>

  <a href="http://woop.ac:81/linux_client_1202.2.tar.bz2" class="download-button">
    <div class="os-icon"><img src="https://cloud.githubusercontent.com/assets/5359646/12221427/acfa7b76-b79a-11e5-966a-f4170ff46303.png"></div>
    <div class="download-content">
      <div>Match Client</div>
      <span class="version">1202.2 (64bits)</span>
    </div>
  </a>
</div>

<h2>Installation</h2>
<h3>Windows</h3>
<p>
  <ul>
      <li>Download and run the <a href="http://woop.ac:81/windows_client_1202.2.exe">windows installer</a>. <br />It is strongly advised to install the game in multi-user mode.</li>
      <li>A "<b>Match Client</b>" shortcut has been created on your desktop. Launch the game and have fun!</li>
  </ul>
</p>
<h3>Linux</h3>
<p>
  <ul>
      <li>Download and extract the <a href="http://woop.ac:81/linux_client_1202.2.tar.bz2">linux package</a>.</li>
      <li>Install the required dependencies :<br />
      For <b>Ubuntu/Debian</b> :
      <pre>sudo apt-get install libsdl1.2debian libsdl-image1.2 zlib1g libogg0 libvorbis0a libopenal1 libcurl3</pre>
      </li>
      <li>Run <b>assaultcube.sh</b></li>
  </ul>
</p>
</article>
<?php
echo $foot;
