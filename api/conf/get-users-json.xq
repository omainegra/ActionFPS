let $json :=
    <json type="array">
        {
            for $user in /user
            let $current-nickname := $user/nickname[not(@to)]
            return <_ type="object">
                <id>{data($user/@id)}</id>
                <gameNickname>{data($user/@game-nickname)}</gameNickname>
                <name>{data($user/@name)}</name>
                <countryCode>{data($user/@country-code)}</countryCode>
                <email>{data($user/@email)}</email>
                <registrationIp>{data($user/@registration-ip)}</registrationIp>
                <registrationDate>{data($user/@registration-date)}</registrationDate>
                <nickname type="object">
                    <nickname>{data($current-nickname/text())}</nickname>
                    <countryCode>{data($current-nickname/@country-code)}</countryCode>
                    <from>{data($current-nickname/@from)}</from>
                </nickname>
                {if ( not(empty($user/nickname[@to]))) then (
                    <previousNicknames type="array">
                        {
                            for $nickname in $user/nickname[@to]
                            return <_ type="object">
                                <nickname>{data($nickname/text())}</nickname>
                                <countryCode>{data($nickname/@country-code)}</countryCode>
                                <from>{data($nickname/@from)}</from>
                                <to>{data($nickname/@to)}</to>
                            </_>
                        }

                    </previousNicknames>) else ()}

            </_>
        }

    </json>

return json:serialize($json)
