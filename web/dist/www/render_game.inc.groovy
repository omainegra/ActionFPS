def render_game_team_player = { game, team, player, Boolean spectator ->
    li(class: (spectator ? "spectator" : "player")) {
        if (player.flags != null) {
            span(class: "score flags", player.flags)
        }
        span(class: "subscore frags", player.frags)
        span(class: "name") {
            if (player.user != null) {
                a(href: "/player/?id=" + player.user, player.name)
            } else {
                span(player.name)
            }
        }
    }
}

def render_game_team = { game, team ->
    def image_url = "http://woop.ac/assets/" + team.name.toLowerCase() + ".png"
    div(class: (team.name + " team")) {
        div(class: "team-header") {
            h3(img(src: image_url))
            div(class: "result") {
                if (team.flags != null) {
                    span(class: "score", team.flags)
                }
                span(class: "subscore", team.frags)
            }
        }
        div(class: "players") {
            ol {
                team.players.each { player ->
                    render_game_team_player(game, team, player, false)
                }
                if (team.spectators != null) {
                    team.spectators.each { spectator ->
                        render_game_team_player(agme, team, spectator, true)
                    }
                }
            }
        }

    }
}

def render_game_header = { game ->

    def ac_link = game.now ? ("assaultcube://" + game.now.server.server) : null

    def demo_link = (game.now && game.server && game.server.contains("aura")) ?
            ('http://woop.ac:81/find-demo.php?time=' + game.id + '&map=' + game.map) : null

    header {
        h2 {
            a(href: (game.id != null ? "/game/?id=" + game.id : null)) {
                yield game.mode + " @ " + game.map + " "
                if (game.now == null) {
                    time(is: "relative-time", datetime: game.endTime, game.endTime)
                }

                if (demo_link != null) {
                    a(target: "_blank", class: "demo-link", href: demo_link)("demo")
                }
                if (ac_link != null) {
                    a(class: "server-link", href = ac_link)("on " + game.now.server.shortName)
                }
                if (game.minRemain != null) {
                    def remainText = game.minRemain == 1 ? "1 minute remains" : (game.minRemain == 0 ? "game finished" : (game.minRemain + " minutes remain"))
                    p(class: "time-remain", remainText)
                }
            }

        }
    }
}

def render_game_players = { players ->
    div(class: "dm-players") {
        ul {
            players.each { player ->
                li(span(player))
            }
        }
    }
}

def render_game_spectators = { spectators ->
    div(class: "spectators") {
        h4("Spectators:")
        ul {
            spectators.each { spectator ->
                li(span(spectator))
            }
        }
    }
}

def render_game_clanwar_info = { clanwar ->
    div(class: "of-clanwar") {
        "part of "
        a(href: ("/clanwar/?id=" + clanwar)) {
            "the Clanwar "
            time(is: "relative-time", datetime = clanwar)
        }
    }
}

def render_game_achievements = { achievements ->
    div(class: "g-achievements") {
        achievements.each { achievement ->
            a(href: ("/player/?id=" + achievement.user))(achievement.text)
            br()
        }
    }
}

def render_game = { game ->
    def article_class = "GameCard game"
    if (game.now) {
        article_class += " isLive"
    }
    if (game.isNew) {
        article_class += " isNew"
    }
    def style_url = "http://woop.ac/assets/maps/" + game.map + ".jpg"
    def style = "background-image: url('" + style_url + "')"
    article(class: article_class, style: style) {
        div(class: "w") {
            render_game_header(game)
            div(class: "teams") {
                game.teams.each { team ->
                    render_game_team(game, team)
                }
            }
            if ( game.players ) {
                render_game_players(game.players)
            }
            if ( game.spectators ) {
                render_game_spectators(game.spectators)
            }
            if ( game.clanwar ) {
                render_game_clanwar_info(game.clanwar)
            }
            if ( game.achievements ) {
                render_game_achievements(game.achievements)
            }
        }
    }
}

render_game(game)