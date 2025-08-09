organization := "com.zaneli"

name := "scalikejdbc-athena"

version := "0.3.0"

val Scala212 = "2.12.20"

scalaVersion := Scala212

crossScalaVersions := Seq(Scala212, "2.13.16", "3.7.2")

val scalikejdbcVersion = "4.3.4"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion % Provided,
  "com.typesafe" % "config" % "1.4.3" % Provided,
  "org.scalatest" %% "scalatest-funspec" % "3.2.19" % Test,
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion % Test,
  "com.h2database" % "h2" % "2.3.232" % Test
)

publishTo := sonatypePublishTo.value

Test / testOptions += Tests.Setup { () =>
  // Call this method to prevent stopping test on the way, but I don't know reason why...
  java.sql.DriverManager.getDrivers
}
