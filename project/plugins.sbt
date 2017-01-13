resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.10")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.5.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

resolvers += Resolver.url(
  "bintray-ScalaWilliam-sbt-plugins",
  url("http://dl.bintray.com/scalawilliam/sbt-plugins"))(
  Resolver.ivyStylePatterns)

addSbtPlugin("com.scalawilliam" % "sbt-maxmind" % "0.1.0")
