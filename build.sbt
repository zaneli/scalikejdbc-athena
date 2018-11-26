organization := "com.zaneli"

name := "scalikejdbc-athena"

version := "0.1.0"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.3.1" % Provided,
  "com.typesafe" % "config" % "1.3.2" % Provided,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "3.3.1" % Test,
  "com.h2database" % "h2" % "1.4.197" % Test
)

publishTo := sonatypePublishTo.value
