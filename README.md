# ActionFPS [![Build Status](https://travis-ci.org/ScalaWilliam/ActionFPS.svg)](https://travis-ci.org/ScalaWilliam/ActionFPS)  [![Workflow](https://badge.waffle.io/ScalaWilliam/actionfps.png?label=ready&title=Ready)](https://waffle.io/ScalaWilliam/actionfps)

* [ActionFPS Homepage](https://actionfps.com/)
* [ActionFPS Wiki](https://github.com/ScalaWilliam/ActionFPS/wiki)
* [ActionFPS Contributor Guide](https://github.com/ScalaWilliam/ActionFPS/wiki/Contributor-Guide)
* [ActionFPS Data Science](https://github.com/ScalaWilliam/ActionFPS/wiki/Data-Science)
* [ActionFPS Development Guide](https://github.com/ScalaWilliam/ActionFPS/wiki/Development-Guide)
* Other repositories:
  * [ActionFPS Game](https://github.com/ActionFPS/ActionFPS-Game)
  * <a href="https://github.com/ActionFPS/game-log-parser">ActionFPS/game-log-parser</a>
  * <a href="https://github.com/ActionFPS/binary-game-parser">ActionFPS/binary-game-parser</a>
  * <a href="https://github.com/ActionFPS/server-pinger">ActionFPS/server-pinger</a>
  * <a href="https://github.com/Paul255/ActionFPS-DiscordBOT">Paul255/ActionFPS-DiscordBOT</a>
* [ActionFPS Blog](https://actionfps.blogspot.com)
* [ActionFPS Discord](https://discord.gg/HYHku8C)
* [ActionFPS GitHub](https://github.com/ScalaWilliam/ActionFPS/)

_add some GIF animations of the game here_


# Quickstart

_More detail at the [ActionFPS Development Guide](https://github.com/ScalaWilliam/ActionFPS/wiki/Development-Guide)_.

1. Install <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html">Oracle JDK 8</a> or <a href="http://openjdk.java.net/install/">OpenJDK 8</a>.
2. Install <a href="www.scala-sbt.org">SBT</a> (<a href="http://www.scala-sbt.org/release/docs/Installing-sbt-on-Mac.html">Mac</a>,
                                                        <a href="http://www.scala-sbt.org/release/docs/Installing-sbt-on-Windows.html">Windows</a>,
                                                        <a href="http://www.scala-sbt.org/release/docs/Installing-sbt-on-Linux.html">Linux</a>).
3. To make changes, in command line execute:<br> `sbt web/run`
4. To make changes involving stats comuptations etc, use:<br>
`sbt 'web/run -Dfull.provider=normal'`

