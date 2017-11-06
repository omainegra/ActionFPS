# server-pinger for ActionFPS

Ping ActionFPS servers via UDP, rebuild the game state from these responses, output as JSON.

Two types of outputs: 'server state' and 'game state' (looks similar to ActionFPS `JsonGame`).

### Running

```bash
$ sbt server-pinger/run
[INFO] [01/14/2017 07:41:44.451] [default-akka.actor.default-dispatcher-2] [akka://default/user/pinger] Starting listener actor for pinger service...
[INFO] [01/14/2017 07:41:44.457] [default-akka.actor.default-dispatcher-4] [akka://default/user/pinger/pinger] Starting pinger actor
{"server":"192.184.63.69:28763","connectName":"califa.actionfps.com 28763","canonicalName":"califa.actionfps.com:28763","shortName":"Califa 28763","description":"\fPCalifa - www.actionfps.com","maxClients":16,"updatedTime":"2017-01-13T23:41:50Z"}
{"when":"right now","reasonablyActive":false,"now":{"server":{"server":"califa.actionfps.com:28763","connectName":"califa.actionfps.com 28763","shortName":"Califa 28763","description":"\fPCalifa - www.actionfps.com"}},"hasFlags":false,"mode":"team deathmatch","minRemain":10,"teams":[],"updatedTime":"2017-01-13T23:41:50Z"}
...
```

### Customizing the server list
Use `-Dservers.csv=<url>`.

Currently it's set to `https://actionfps.com/servers/?format=csv`.

