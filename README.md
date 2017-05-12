# ActionFPS Portal [![Build Status](https://travis-ci.org/ScalaWilliam/ActionFPS.svg)](https://travis-ci.org/ScalaWilliam/ActionFPS)  [![Workflow](https://badge.waffle.io/ScalaWilliam/actionfps.png?label=ready&title=Ready)](https://waffle.io/ScalaWilliam/actionfps)

> [ActionFPS website](https://actionfps.com/)

## Quickstart

_More detail: [ActionFPS Portal Development Guide](https://docs.actionfps.com/portal-development-guide.html)_

### Prerequisites
Make sure to install:
- <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html">Oracle JDK 8</a> or <a href="http://openjdk.java.net/install/">OpenJDK 8</a>.
- <a href="www.scala-sbt.org">SBT</a>: (<a href="http://www.scala-sbt.org/release/docs/Installing-sbt-on-Mac.html">Mac</a>,
                                                        <a href="http://www.scala-sbt.org/release/docs/Installing-sbt-on-Windows.html">Windows</a>,
                                                        <a href="http://www.scala-sbt.org/release/docs/Installing-sbt-on-Linux.html">Linux</a>).
### Development

```
$ sbt web/run
```

#### In-memory results cache
When developing the front-end, iterations may be slow.
This is because some results are recomputed. If you'd like to cache them, use:

```
$ sbt 'set inMemoryCache in web := true' web/run
```
