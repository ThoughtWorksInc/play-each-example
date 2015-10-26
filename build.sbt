enablePlugins(PlayScala)

libraryDependencies += ws

libraryDependencies += "com.thoughtworks.each" %% "each" % "0.5.1"

routesGenerator := InjectedRoutesGenerator

scalaVersion := "2.11.7"