<?php
require("../render.inc.php");
?>
    <div id="questions">

        <h1>Register &amp; Play!</h1>

        <h2>1. Log in with your Google account</h2>
        <div class="g-signin2" data-onsuccess="onSignIn"></div>

        <div id="reg-form">
            <h2>2. Register an ActionFPS account</h2>

            <div id="register-waiting">
                <p>Please sign in</p>
            </div>

            <div id="register-signedin">

                <form class="pure-form pure-form-aligned" id="reg_form" method="post" enctype="multipart/form-data"
                      action="https://script.google.com/macros/s/AKfycbw2na0_P4atptBWe_AXn2TJECge8POwV-3ai5QGJ2hd25TffWtY/exec">
                    <fieldset>

                        <div class="pure-control-group">
                            <label for="nickname">Nickname</label>
                            <input name="nickname" id="field-nickname" type="text"
                                   placeholder="In-game nickname, e.g. w00p|Drakas" pattern="[^\s]+{3,15}">
                            <p>Anything valid, 3 characters minimum.</p>
                        </div>

                        <div class="pure-control-group">
                            <label for="name">Username</label>
                            <input name="name" id="field-username" type="text"
                                   placeholder="Simple name, without clan tag, e.g. Drakas" pattern="[A-Z]?[a-z]{3,15}">
                            <p>First letter can be uppercase, the rest must be lowercase</p>
                        </div>

                        <div class="pure-control-group">
                            <label for="id">User ID</label>
                            <input type="text" id="field-id" name="id"
                                   placeholder="3 or more a-z characters, e.g. drakas" pattern="[a-z]{3,10}"/>
                            <p>Lowercase letters only.</p>
                            <p>Your profile will be available at <code>https://actionfps.com/player/?id=<span
                                        id="player-id-value"></span></code></p>
                        </div>
                        <input type="hidden" name="token" id="token"/>
                        <input type="hidden" name="redirect" value="true"/>

                        <div class="pure-controls">
                            <button type="submit" class="pure-button pure-button-primary">Register</button>
                        </div>
                    </fieldset>
                </form>

            </div>

        </div>


        <h2>3. Download the game</h2>

        <p><a href="http://woop.ac/client/windows_client_1202.2.exe" class="button-error pure-button">Download ActionFPS
                Match Client</a></p>
        <p>Windows, 44MB download</p>

        <p><img src="https://cloud.githubusercontent.com/assets/2464813/12762693/4e9df7dc-c9e8-11e5-8a2c-704f4af9a25f.png"/></p>

        <h2>4. Play it</h2>
        <p><a href="/servers/" class="button-info pure-button">Server list</a></p>
        <p>Just click on a server you'd like to play on</p>

        <h2>5. Join us on TeamSpeak</h2>
        <p><a href="ts3server://aura.woop.ac:9988"><img
                    src="https://cloud.githubusercontent.com/assets/2464813/12760873/99820c6e-c9e0-11e5-832d-00c1b61c2e4c.png"/></a>
        </p>
        <p><a href="http://www.teamspeak.com/?page=downloads" target="_blank">TeamSpeak is free VoIP for gaming.</a></p>

        <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>

        <meta name="google-signin-client_id"
              content="566822418457-bqerpiju1kajn53d8qumc6o8t2mn0ai9.apps.googleusercontent.com">
        <script src="https://apis.google.com/js/platform.js" async defer></script>
        <script type="text/javascript">
            $("#field-nickname").on("keyup", function (v) {
                var nickname = $(v.target).val();
                var newUsername = nickname.replace(/[^A-Za-z]/g, '');
                newUsername = newUsername[0] + newUsername.substring(1).toLowerCase();
                var newId = newUsername.toLowerCase();
                $('#field-id').val(newId);
                $('#field-username').val(newUsername);
                $('#player-id-value').text(newId);
            })
            $("#field-id").on("keyup", function (v) {
                $('#player-id-value').text($(v.target).val());
            })
        </script>
        <script type="text/javascript">
            var rootUrl = "https://script.google.com/macros/s/AKfycbw2na0_P4atptBWe_AXn2TJECge8POwV-3ai5QGJ2hd25TffWtY/exec";
            function onSignIn(googleUser) {
                $("#reg-form").addClass("signed-in");
                var profile = googleUser.getAuthResponse();
                $("#token").val(profile.id_token);
                var queryUrl = rootUrl + "?email=" + googleUser.getBasicProfile().getEmail();
                $.get(queryUrl).then(function (x) {
                    if (x === false) {
                    } else if ("id" in x) {
                        $.cookie("af_id", x.id, {path: '/', expires: 100});
                        $.cookie("af_name", x.name, {path: '/', expires: 100});
                        $('#login_welcome a').attr("href", "/player/?id=" + x.id).text(x.name);
                        $('#welcome-user, #log-in').attr("href", "/player/?id=" + x.id).text(x.name);
                        $('#login_welcome').css("display", "block");
                        $('#reg-form').remove()
                    }
                })
            }
        </script>
    </div>
<?php echo $foot;