# ActionFPS

[![Build Status](https://travis-ci.org/ScalaWilliam/ActionFPS.svg)](https://travis-ci.org/ScalaWilliam/ActionFPS)
[![Join the chat at https://gitter.im/ScalaWilliam/ActionFPS](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ScalaWilliam/actionfps?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Workflow](https://badge.waffle.io/ScalaWilliam/actionfps.png?label=ready&title=Ready)](https://waffle.io/ScalaWilliam/actionfps)

* Now open source.
* https://actionfps.com/
* Also see http://duel.gg/


# Developing

Use IntelliJ Community Edition: https://www.jetbrains.com/idea/download/

Simply import the directory as an 'SBT' project

Install SBT: http://www.scala-sbt.org/download.html

Install PHP7: https://bjornjohansen.no/upgrade-to-php7

In one console run:
```
sbt startPHP
```

In the other console run:

```
sbt web/run
```

# Running tests

```
sbt clean test
```

[![Throughput Graph](https://graphs.waffle.io/ScalaWilliam/actionfps/throughput.svg)](https://waffle.io/ScalaWilliam/actionfps/metrics)

# Technology Choices

* __Scala__ for data processing and Play framework: solid, stable toolkit for dealing with complex data.

## DevOps
Continuous Deployment: master --> <https://git.watch/> --> build & restart. Simple monolithic deployment.

