enablePlugins(GatlingPlugin)

scalaVersion := "2.11.8"

resolvers += "justwrote" at "http://repo.justwrote.it/releases/"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.5" % "test"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % "2.2.5" % "test"
libraryDependencies += "it.justwrote" %% "scala-faker" % "0.3"
