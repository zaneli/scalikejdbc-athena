sonatypeProfileName := "com.zaneli"

publishMavenStyle := true
Test / publishArtifact := false

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting(user = "zaneli", repository = "scalikejdbc-athena", email = "shun.otani@gmail.com"))

developers := List(
  Developer(id = "zaneli", name = "Shunsuke Otani", email = "shun.otani@gmail.com", url = url("https://www.zaneli.com/"))
)
