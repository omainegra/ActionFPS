# ActionFPS

[![Build Status](https://travis-ci.org/ScalaWilliam/ActionFPS.svg)](https://travis-ci.org/ScalaWilliam/ActionFPS)
[![Join the chat at https://gitter.im/ScalaWilliam/ActionFPS](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ScalaWilliam/actionfps?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Workflow](https://badge.waffle.io/ScalaWilliam/actionfps.png?label=ready&title=Ready)](https://waffle.io/ScalaWilliam/actionfps)

* Now open source.
* http://actionfps.com/
* Previously known as http://woop.ac/
* Also see http://duel.gg/

# Technology Choices

* __Scala__ for data processing and Play framework: solid, stable toolkit for dealing with complex data.
* __PHP__ for the server-side frontend: fast deploys, quick language for web interfaces.

# Compiling

Install SBT: http://www.scala-sbt.org/download.html

```
sbt clean test dist
```

# Running frontend

```
cd www/
php -S localhost:8888
```

# Coding it

Use IntelliJ: https://www.jetbrains.com/idea/download/

The most important module is 'api', which you start with:

```
sbt api/run
```
