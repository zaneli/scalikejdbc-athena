# scalikejdbc-athena

Library for using [Amazon Athena](https://aws.amazon.com/athena/) JDBC Driver with [ScalikeJDBC](http://scalikejdbc.org/)

[![Build Status](https://api.travis-ci.org/zaneli/scalikejdbc-athena.png?branch=master)](https://travis-ci.org/zaneli/scalikejdbc-athena)

## setup

- Download [Athena JDBC Driver](https://docs.aws.amazon.com/athena/latest/ug/athena-jdbc-driver.html)
```sh
> mkdir lib
> curl -L -O https://s3.amazonaws.com/athena-downloads/drivers/AthenaJDBC41-1.1.0.jar
> mv AthenaJDBC41-1.1.0.jar lib/
```

- Configure the JDBC Driver Options on `resources/application.conf`

```
athena {
  default {
    driver="com.amazonaws.athena.jdbc.AthenaDriver"
    url="jdbc:awsathena://athena.{REGION}.amazonaws.com:443"
    s3_staging_dir="s3://query-results-bucket/folder/"
    aws_credentials_provider_class="com.amazonaws.auth.profile.ProfileCredentialsProvider"
    log_path="logs/application.log"
  }
}
```

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

### Delete `s3_staging_dir` after run query

* set `s3_staging_dir_prefix` instead of `s3_staging_dir`
```
athena {
  default {
    driver="com.amazonaws.athena.jdbc.AthenaDriver"
    url="jdbc:awsathena://athena.{REGION}.amazonaws.com:443"
    s3_staging_dir_prefix="s3://query-results-bucket/folder"
    aws_credentials_provider_class="com.amazonaws.auth.profile.ProfileCredentialsProvider"
    log_path="logs/application.log"
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
      val delReq = new DeleteObjectsRequest(bucketName)
      delReq.setKeys(keys.asJava)
      s3Client.deleteObjects(delReq)
  }
}
```

---

scalikejdbc-athena is inspired by [scalikejdbc-bigquery](https://github.com/ocadaruma/scalikejdbc-bigquery).
