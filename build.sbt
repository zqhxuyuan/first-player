name := """first-player"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "net.sf.barcode4j" % "barcode4j" % "2.0",
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3",
  "org.webjars" % "font-awesome" % "4.6.3"
)

//libraryDependencies += "com.typesafe.play" % "anorm_2.11" % "2.4.0"

//libraryDependencies += "org.squeryl" % "squeryl_2.11" % "0.9.7"

//libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "1.6.2"
