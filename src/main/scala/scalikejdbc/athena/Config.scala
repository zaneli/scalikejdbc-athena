package scalikejdbc.athena

import java.util.{Properties, UUID}

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

private[athena] class Config(dbName: Any) {

  private[this] val prefix = "db." + (dbName match {
    case s: Symbol => s.name
    case s: String => s
    case o => throw new ConfigException("")
  })

  private[this] val config = ConfigFactory.load()

  private[this] val optionalNames = Seq(
    "query_results_encryption_option", "query_results_aws_kms_key",
    "aws_credentials_provider_class", "aws_credentials_provider_arguments",
    "max_error_retries", "connection_timeout", "socket_timeout",
    "retry_base_delay", "retry_max_backoff_time", "log_path", "log_level"
  )
  private[this] val attributeNames = Seq(
    "url", "driver", "s3_staging_dir", "s3_staging_dir_prefix",
  ) ++ optionalNames

  private[this] val map = config.getConfig(prefix).entrySet.asScala.map(_.getKey).collect {
    case key if attributeNames.contains(key) =>
      key -> config.getString(s"$prefix.$key")
  }.toMap

  map.get("driver").foreach(Class.forName)

  private[this] lazy val stagingDirSuffix = UUID.randomUUID().toString

  private[athena] lazy val url: String = map.getOrElse("url", throw new ConfigException(""))

  private[athena] lazy val options: Properties = {
    val p = new Properties()
    (map.get("s3_staging_dir"), map.get("s3_staging_dir_prefix")) match {
      case (Some(_), Some(_)) => throw new ConfigException("")
      case (Some(v), _) => p.put("s3_staging_dir", v)
      case (_, Some(v)) => p.put("s3_staging_dir", s"$v/$stagingDirSuffix")
      case _ => throw new ConfigException("")
    }
    optionalNames.foreach { name =>
      map.get(name).foreach(value => p.put(name, value))
    }
    p
  }

  private[athena] lazy val getTmpStagingDir: Option[String] = {
    map.get("s3_staging_dir_prefix").map { v =>
      s"$v/$stagingDirSuffix"
    }
  }
}

class ConfigException(message: String) extends Exception(message)
