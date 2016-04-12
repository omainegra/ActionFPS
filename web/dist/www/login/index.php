<?php
require("../render.inc.php");
?>
<div id="questions">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
  
<meta name="google-signin-client_id" content="566822418457-bqerpiju1kajn53d8qumc6o8t2mn0ai9.apps.googleusercontent.com">
<script src="https://apis.google.com/js/platform.js" async defer></script>
<div class="g-signin2" data-onsuccess="onSignIn"></div>
<script type="text/javascript">
var rootUrl = "https://script.google.com/macros/s/AKfycbw2na0_P4atptBWe_AXn2TJECge8POwV-3ai5QGJ2hd25TffWtY/exec";
function onSignIn(googleUser) {
  var profile = googleUser.getAuthResponse();
  $("#token").val(profile.id_token);
  var queryUrl = rootUrl + "?email=" + googleUser.getBasicProfile().getEmail();
  $.get(queryUrl).then(function(x) {
	  if ( x === false ) {
		  $("#reg_form").css("display", "block");
	  } else if ( "id" in x ) {
      $.cookie("af_id", x.id, { path:'/', expires: 100 });
      $.cookie("af_name", x.name, { path:'/', expires: 100 });
      $('#login_welcome a').attr("href", "/player/?id=" + x.id).text(x.name);
      $('#welcome-user, #log-in').attr("href", "/player/?id=" + x.id).text(x.name);
      $('#login_welcome').css("display", "block");
	  }
  })
}
</script>
<div style="display:none" id="login_welcome">
  <p>Welcome, <a href="" id="welcome-user"></a></p>
  <p>We're still developing the software, so new profiles don't appear to be working just yet.</p>
  </div>
<form style="display:none" id="reg_form" method="post" enctype="multipart/form-data" action="https://script.google.com/macros/s/AKfycbw2na0_P4atptBWe_AXn2TJECge8POwV-3ai5QGJ2hd25TffWtY/exec">
<h2>Register account</h2>
<label>
	Your user ID:
<input type="text" name="id" placeholder="3 or more a-z characters, e.g. drakas" pattern="[a-z]{3,}"/>

</label>
<br/>
<label>
	Your username:
<input type="text" name="name" placeholder="Simple name, can be capitalised, 3 or more a-z characters, e.g. Drakas" pattern="[A-Z]?[a-z]{3,}"/>
</label>
<br/>
<label>
	In-game nickname:
<input type="text" name="nickname" placeholder="In-game nickname, e.g. w00p|Drakas" pattern="[^\s]+{3,}"/>
</label>
<br/>
<input type="hidden" name="token" id="token"/>
<input type="hidden" name="redirect" value="true"/>
<button type="submit">Submit it</button>
</form>
<script type="text/javascript">
function sub_me() {
$.post(rootUrl, $('#reg_form').serialize()).then(function(x) {
	console.log("Got it", x);
});
}
</script>
</div>
<?php echo $foot; 