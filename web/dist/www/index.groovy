layout './wut.groovy',
        title: 'ABC',
        bodyContents: contents {

//            div(id: "live-events") {
//                ol(class: "LiveEvents live-events") {
//                    events.each { event ->
//                        li {
//                            a(href: "/player/?id=" + event.user, event.text)
//                            span(class: when) {
//                                time(is: "relative-time", datetime = event.date, event.date)
//                            }
//                        }
//                    }
//                }
//            }
//
            div(id: "games") {
//                div(id: "dynamic-games")()
//                div(id: "new-games")()
//                div(id: "latest-clanwar")(clanwar)
                div(id: "existing-games", games)
            }

            script(src: "https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js") {}
            script(src: "/assets/live/live.js") {}

        }
