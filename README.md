# ActionFPS

[![Build Status](https://travis-ci.org/ScalaWilliam/ActionFPS.svg)](https://travis-ci.org/ScalaWilliam/ActionFPS)
[![Join the chat at https://gitter.im/ScalaWilliam/ActionFPS](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ScalaWilliam/actionfps?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Workflow](https://badge.waffle.io/ScalaWilliam/actionfps.png?label=ready&title=Ready)](https://waffle.io/ScalaWilliam/actionfps)

* Now open source.
* http://actionfps.com/
* Previously known as http://woop.ac/
* Also see http://duel.gg/
* Contributors very much welcome. Start with Issues.

[![Throughput Graph](https://graphs.waffle.io/ScalaWilliam/actionfps/throughput.svg)](https://waffle.io/ScalaWilliam/actionfps/metrics) 


# Technology Choices

* __Scala__ for data processing and Play framework: solid, stable toolkit for dealing with complex data.
* __PHP__ for the server-side frontend: speedy development for dynamic websites.

## DevOps
We have Travis CI. We also have Continuous Deployment from master to our dedicated CentOS 7 server via Amazon SQS and GitHub Web Hooks. We are targeting a monolothic deployment with highly modular code. We use SBT for building everything. It is the superior tool of choice.


# Prerequisite

Install SBT: http://www.scala-sbt.org/download.html

Install php7.0: https://bjornjohansen.no/upgrade-to-php7

# Running frontend

```
sbt web/run &
cd web/dist/www && php -S localhost:8888
```

# Running tests

```
sbt clean test dist
```

# Coding it

Use IntelliJ: https://www.jetbrains.com/idea/download/
Import the build.sbt file from Import Project from Existing Sources... menu
