name := """first-player"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  evolutions,
  "com.h2database" % "h2" % "1.3.170",
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "net.sf.barcode4j" % "barcode4j" % "2.0",
  "org.webjars" % "font-awesome" % "4.6.3"
)

libraryDependencies += "com.adrianhurt" % "play-bootstrap_2.11" % "1.1-P25-B3"

//libraryDependencies += "com.typesafe.play" % "play-slick_2.11" % "2.0.2"

//libraryDependencies += "com.typesafe.play" % "play-slick_2.10" % "1.1.1"

//libraryDependencies += "org.squeryl" % "squeryl_2.11" % "0.9.7"

//libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "1.6.2"
