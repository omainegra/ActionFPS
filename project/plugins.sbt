resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.6")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

resolvers += Resolver.url(
  "bintray-ScalaWilliam-sbt-plugins",
  url("http://dl.bintray.com/scalawilliam/sbt-plugins"))(
  Resolver.ivyStylePatterns)

addSbtPlugin("com.scalawilliam" % "sbt-maxmind" % "0.1.0")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.2.24")
