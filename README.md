# scalikejdbc-athena

Library for using [Amazon Athena](https://aws.amazon.com/athena/) JDBC Driver with [ScalikeJDBC](http://scalikejdbc.org/)

![CI Status](https://github.com/zaneli/scalikejdbc-athena/actions/workflows/ci.yml/badge.svg)

## setup

- Download Athena JDBC 3.x driver
  - This library supports both Athena (https://docs.aws.amazon.com/athena/latest/ug/jdbc-v3-driver.html) and https://docs.aws.amazon.com/athena/latest/ug/jdbc-v2.html.  
    If you encounter problems with a particular version, please feel free to [report it](https://github.com/zaneli/scalikejdbc-athena/issues).
```sh
> mkdir lib
> pushd lib
> curl -L -O https://downloads.athena.us-east-1.amazonaws.com/drivers/JDBC/3.5.1/athena-jdbc-3.5.1-with-dependencies.jar
> popd
```

- Add library dependencies to sbt build settings
```scala
libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",
  "com.zaneli" %% "scalikejdbc-athena" % "0.3.0"
)
```

- Configure the JDBC Driver Options on `resources/application.conf`

```
# v3 driver
athena {
  default {
    driver="com.amazon.athena.jdbc.AthenaDriver"
    url="jdbc:awsathena://AwsRegion={REGION}"
    readOnly="false"
    S3OutputLocation="s3://query-results-bucket/folder/"
    AwsCredentialsProviderClass="DefaultChain"
    LogPath="logs/application.log"
    LogLevel=3
  }
}

# v2 driver
athena {
  default {
    driver="com.simba.athena.jdbc.Driver"
    url="jdbc:awsathena://AwsRegion={REGION}"
    readOnly="false"
    S3OutputLocation="s3://query-results-bucket/folder/"
    AwsCredentialsProviderClass="com.simba.athena.amazonaws.auth.profile.ProfileCredentialsProvider"
    LogPath="logs/application.log"
    LogLevel=3
  }
}
```

If you need to update partitions etc., set `readOnly="false"`

## Usage

### Run query

```scala
import scalikejdbc._
import scalikejdbc.athena._

val name = "elb_demo_001"
DB.athena { implicit s =>
  val r = sql"""
          |SELECT * FROM default.elb_logs_raw_native
          |WHERE elb_name = $name LIMIT 10;
         """.stripMargin.map(_.toMap).list.apply()
  r.foreach(println)
}
```

### Delete `S3OutputLocation` after run query

* set `S3OutputLocationPrefix` instead of `S3OutputLocation`
```
athena {
  default {
    driver="com.amazon.athena.jdbc.AthenaDriver"
    url="jdbc:awsathena://AwsRegion={REGION}"
    readOnly="false"
    S3OutputLocationPrefix="s3://query-results-bucket/folder"
    AwsCredentialsProviderClass="DefaultChain"
    LogPath="logs/application.log"
    LogLevel=3
  }
}
```

* use [aws-java-sdk-s3](https://docs.aws.amazon.com/AmazonS3/latest/dev/DeletingMultipleObjectsUsingJava.html)

```scala
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.DeleteObjectsRequest

import scalikejdbc._
import scalikejdbc.athena._

import scala.collection.JavaConverters._

val s3Client = AmazonS3ClientBuilder.standard().withCredentials(new ProfileCredentialsProvider()).build()
val regex = """s3://(.+?)/(.+)""".r

DB.athena { implicit s =>
  val r = sql"...".map(_.toMap).list.apply()
  r.foreach(println)

  s.getTmpStagingDir.foreach { // Some("s3://query-results-bucket/folder/${java.util.UUID.randomUUID}")
    case regex(bucketName, path) =>
      val keys = s3Client.listObjects(bucketName, path).getObjectSummaries.asScala
        .map(s => new DeleteObjectsRequest.KeyVersion(s.getKey))
      if (keys.nonEmpty) {
        val delReq = new DeleteObjectsRequest(bucketName)
        delReq.setKeys(keys.asJava)
        s3Client.deleteObjects(delReq)
      }
  }
}
```

---

scalikejdbc-athena is inspired by [scalikejdbc-bigquery](https://github.com/ocadaruma/scalikejdbc-bigquery).
