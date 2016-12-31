var OpenMatchPlayers = Java.type('tl.OpenMatchPlayers');

function Tournament() {
}

Tournament.prototype.getOpenMatches = function ()
    this.tournament.matches
        .map(function (m) m.match)
        .filter(function (m) m.state == "open");

Tournament.prototype.getParticipant = function (participantId)
    this.tournament.participants
        .map(function (p) p.participant)
        .filter(function (p) p.id == participantId)
        .pop();

//noinspection JSUnusedGlobalSymbols
Tournament.prototype.getOpenMatchesPlayers = function ()
    this.getOpenMatches().map(function (m) {
        return new OpenMatchPlayers(
            m.id,
            this.getParticipant(m.player1_id).name,
            this.getParticipant(m.player2_id).name
        )
    }.bind(this));

Tournament.fromObject = function (obj) {
    obj.__proto__ = Tournament.prototype;
    return obj;
};

Tournament.fromJSON = function (jsonString)
    Tournament.fromObject(JSON.parse(jsonString));
