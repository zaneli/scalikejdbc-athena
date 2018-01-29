package scalikejdbc.athena

import java.util.{Properties, UUID}

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

class Config(dbName: Any) {
  import Config._

  private[this] val prefix = "athena." + (dbName match {
    case s: Symbol => s.name
    case s: String => s
    case o => throw new ConfigException(s"unexpected db name type: value=$o, type=${o.getClass}")
  })

  private[this] val config = ConfigFactory.load()

  private[this] val optionalNames = Seq(
    QueryResultsEncryptionOption, QueryResultsAWSKmsKey,
    AWSCredentialsProviderClass, AWSCredentialsProviderArguments,
    MaxErrorRetries, ConnectionTimeout, SocketTimeout,
    RetryBaseDelay, RetryMaxBackoffTime, LogPath, LogLevel
  )
  private[this] val attributeNames = Seq(Url, Driver, S3StagingDir, S3StagingDirPrefix) ++ optionalNames

  private[this] val map = if (config.hasPath(prefix)) {
    config.getConfig(prefix).entrySet.asScala.map(_.getKey).collect {
      case key if attributeNames.contains(key) =>
        key -> config.getString(s"$prefix.$key")
    }.toMap
  } else {
    throw new ConfigException(s"no configuration setting: key=$prefix")
  }

  map.get(Driver).foreach(Class.forName)

  private[athena] lazy val url: String = map.getOrElse(Url, throw new ConfigException(s"no configuration setting: key=$prefix.$Url"))

  private[athena] lazy val options: Properties = {
    val p = new Properties()
    (map.get(S3StagingDir), map.get(S3StagingDirPrefix)) match {
      case (Some(d), Some(p)) => throw new ConfigException(s"duplicate settings: $prefix.$S3StagingDir=$d, $prefix.$S3StagingDirPrefix=$p")
      case (Some(v), _) => p.setProperty(S3StagingDir, v)
      case (_, Some(v)) => p.setProperty(S3StagingDir, s"$v/${UUID.randomUUID()}")
      case _ => throw new ConfigException(s"no configuration setting: key=$prefix.$S3StagingDir, $prefix.$S3StagingDirPrefix")
    }
    optionalNames.foreach { name =>
      map.get(name).foreach(value => p.setProperty(name, value))
    }
    p
  }

  private[athena] lazy val getTmpStagingDir: Option[String] = {
    if (map.contains(S3StagingDirPrefix)) {
      Option(options.getProperty(S3StagingDir))
    } else {
      None
    }
  }
}

object Config {
  val Url = "url"
  val Driver = "driver"

  val S3StagingDir = "s3_staging_dir"
  val S3StagingDirPrefix = "s3_staging_dir_prefix"

  val QueryResultsEncryptionOption = "query_results_encryption_option"
  val QueryResultsAWSKmsKey = "query_results_aws_kms_key"
  val AWSCredentialsProviderClass = "aws_credentials_provider_class"
  val AWSCredentialsProviderArguments = "aws_credentials_provider_arguments"
  val MaxErrorRetries = "max_error_retries"
  val ConnectionTimeout = "connection_timeout"
  val SocketTimeout = "socket_timeout"
  val RetryBaseDelay = "retry_base_delay"
  val RetryMaxBackoffTime = "retry_max_backoff_time"
  val LogPath = "log_path"
  val LogLevel = "log_level"
}

class ConfigException(message: String) extends Exception(message)
