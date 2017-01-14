# Game Log Parser for AssaultCube/ActionFPS

This project turns log lines from AssaultCube/ActionFPS into games.
There is no 'game' data format outputted by the server which is why we need to do it ourselves.

## Format problems

There are many problems with the AC data format that we shall overcome.

They will have to be fixed in the server itself though.


1. No server ID/hostname. There should be a server ID & hostname.
2. No timestamp. There should be one.
3. Where there is a timestamp per message, it is of format `Sep 01 15:51:11`, does not have year or time offset. Should contain ISO8601 with zone and offset.

## Current syslog format

Date: 2016-05-14T13:00:40.944Z, Server: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#1999], Payload: Team  CLA:  3 players,    5 frags

## Server ID / hostname problem

As the logs are received via syslog, we actually receive a server name in form such as:

```
62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#1999]
```

Unfortunately this forces us to have to maintain a list of mappings from this 'id' to a server such as `62.210.131.155:1999`
as the format is varying and unpredictable.
 
## No timestamp problem

As we are using syslog to collect the events, we make use of the syslog timestamp
 to attach a timestamp to the full message, to produce in syslog-ac the following:
```
Date: 2016-05-14T13:00:40.944Z, Server: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#1999], Payload: Team  CLA:  3 players,    5 frags
```

If we wanted to rebuild a somewhat more accurate world view, a current timestamp
can be obtained at a message that contains `Status at 01-09-2016 15:51:21`. 
However this is missing a time offset and time zone.

With this we'd be able to attach the Status timestamp for all the following messages
 until a new Status is received (once a minute).

## Where there is a timestamp

It's in the form of `Sep 01 15:51:11`. Non-standard, missing year and time zone/offset.
We can combine it with the Status message above, to get a local date time.

This approach is being used for the ladder parser, to get messages in the form:

```
2016-04-27T08:26:08 Status at 27-04-2016 08:26:08: 0 remote clients, 0.0 send, 0.0 rec (K/sec); Ping: #0|0|0; CSL: #0|0|0 (bytes)
2016-04-27T08:26:08 master server registration succeeded
```

# Ideal format

Ideal format produced by every single server should look like:

```
2017-01-14T09:38:14.406+08:00[Asia/Singapore] woop.ac:1999 62.210.131.155:1999 master server registration succeeded
2017-01-14T09:38:14[UTC] woop.ac:2999 62.210.131.155:2999 master server registration succeeded
```

This gives us:
* String-sortable, and thus mergeable data. We can merge multiple servers data easily.
* May contain server local time information (although we always prefer UTC anyway).
* Contains FQDN of the server name. This is the human friendly version and people typically refer to this
when they are having issues for example.
* Contains the full IP of the server name. This allows us to know what IP that hostname had at the time
as IPs may change due to migrations etc.

So perhaps this is the common format we should look at producing from now on, in all cases.

Currently we don't support the 'Ideal format' for any of the tools.


## Tools

### <a href="blob/master/iso_local_datetime.pl">iso_local_datetime.pl</a>

```bash
./iso_local_datetime.pl << EOF
Sep 01 15:51:21 Status at 01-09-2016 15:51:21: 0 ...
Sep 01 16:21:15 [195.236.145.100] client connected
EOF
2016-09-01T15:51:21 Status at 01-09-2016 15:51:21: 0 ...
2016-09-01T16:21:15 [195.236.145.100] client connected
```

### <a href="blob/master/recover_time.pl">recover_time.pl</a>

```bash
./recover_time.pl <<EOF
heredoc> Status at 01-09-2016 15:51:21: 0 ...
heredoc> [195.236.145.100] client connected
heredoc> EOF
2016-09-01T15:51:21 Status at 01-09-2016 15:51:21: 0 ...
2016-09-01T15:51:21 [195.236.145.100] client connected
```

### <a href="blob/master/individual_to_common_format.pl">individual_to_common_format.pl</a>

```bash
./individual_to_common_format.pl --server-name=woop.ac:1999 --server-ip=62.210.131.155:1999 <<EOF
2016-09-01T15:51:21 Status at 01-09-2016 15:51:21: 0 ...
2016-09-01T16:21:15 [195.236.145.100] client connected
EOF
2016-09-01T15:51:21Z woop.ac:1999 62.210.131.155:1999 Status at 01-09-2016 15:51:21: 0 ...
2016-09-01T16:21:15Z woop.ac:1999 62.210.131.155:1999 [195.236.145.100] client connected
```

### Combined example

```bash
./iso_local_datetime.pl <<EOF |
pipe heredoc> Sep 01 15:51:21 Status at 01-09-2016 15:51:21: 0 ...
pipe heredoc> Sep 01 16:21:15 [195.236.145.100] client connected
pipe heredoc> EOF
pipe> ./individual_to_common_format.pl --server-name=woop.ac:1999 --server-ip=62.210.131.155:1999
2016-09-01T15:51:21Z woop.ac:1999 62.210.131.155:1999 Status at 01-09-2016 15:51:21: 0 ...
2016-09-01T16:21:15Z woop.ac:1999 62.210.131.155:1999 [195.236.145.100] client connected
```
