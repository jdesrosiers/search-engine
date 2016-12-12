name := """search-engine"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  "org.scalatestplus" %% "play" % "1.4.0" % "test",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.6.3",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.typelevel" %% "scalaz-outlaws" % "0.2"


)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/stew/snapshots"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
