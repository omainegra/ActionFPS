scalaVersion := "2.11.7"
libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-streams" % "2.3.0"
libraryDependencies += json
libraryDependencies += "com.propensive" %% "rapture-json" % "2.0.0-M5"
libraryDependencies += "com.propensive" %% "rapture-json-circe" % "2.0.0-M5"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.1"

run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))

runMain in Compile <<= Defaults.runMainTask(fullClasspath in Compile, runner in(Compile, run))

libraryDependencies += "org.typelevel" %% "cats" % "0.6.0"
libraryDependencies += "org.typelevel" %% "kittens" % "1.0.0-M3"
