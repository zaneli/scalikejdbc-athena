organization := "com.zaneli"

name := "scalikejdbc-athena"

version := "0.0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.1.0" % Provided,
  "com.typesafe" % "config" % "1.3.2" % Provided
)
