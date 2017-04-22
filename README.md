# ActionFPS Portal [![Build Status](https://travis-ci.org/ScalaWilliam/ActionFPS.svg)](https://travis-ci.org/ScalaWilliam/ActionFPS)  [![Workflow](https://badge.waffle.io/ScalaWilliam/actionfps.png?label=ready&title=Ready)](https://waffle.io/ScalaWilliam/actionfps)

> [ActionFPS website](https://actionfps.com/)

_See [ActionFPS Portal Development Guide](https://docs.actionfps.com/Portal-Development-Guide.html)_

## Quickstart

1. Install <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html">Oracle JDK 8</a> or <a href="http://openjdk.java.net/install/">OpenJDK 8</a>.
2. Install <a href="www.scala-sbt.org">SBT</a> (<a href="http://www.scala-sbt.org/release/docs/Installing-sbt-on-Mac.html">Mac</a>,
                                                        <a href="http://www.scala-sbt.org/release/docs/Installing-sbt-on-Windows.html">Windows</a>,
                                                        <a href="http://www.scala-sbt.org/release/docs/Installing-sbt-on-Linux.html">Linux</a>).
3. To make changes, in command line execute:<br> `sbt web/run`
4. To make changes involving stats comuptations etc, use:<br>
`sbt 'web/run -Dfull.provider=normal'`

