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
#### Get the reference data
```
$ make reference-data
```

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

#### Benchmarks

We use [sbt-jmh](https://github.com/ktoso/sbt-jmh).

```
$ sbt 'benchmark/jmh:run -prof jmh.extras.JFR -t 1 -f 1 -wi 0 -i 1 .*FullIteratorBenchmark.*'
... 
[info] Flight Recording output saved to: 
[info]   /home/.../some.jfr
```

You can open up this file in [Java Mission Control](https://www.youtube.com/watch?v=qytuEgVmhsI)
(`jmc`) to analyse performance.