organization := "com.zaneli"

name := "scalikejdbc-athena"

version := "0.2.0"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.3.4" % Provided,
  "com.typesafe" % "config" % "1.3.3" % Provided,
  "org.scalatest" %% "scalatest" % "3.0.7" % Test,
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "3.3.4" % Test,
  "com.h2database" % "h2" % "1.4.199" % Test
)

publishTo := sonatypePublishTo.value
