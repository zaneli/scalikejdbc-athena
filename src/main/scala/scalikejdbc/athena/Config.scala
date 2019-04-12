package scalikejdbc.athena

import java.util.{Properties, UUID}

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.util.Try

class Config(dbName: Any) {
  import Config._

  private[this] val prefix = "athena." + (dbName match {
    case s: Symbol => s.name
    case s: String => s
    case o => throw new ConfigException(s"unexpected db name type: value=$o, type=${o.getClass}")
  })

  private[this] val config = ConfigFactory.load()

  private[this] val optionalNames = Seq(
    AwsCredentialsProviderArguments, AwsCredentialsProviderClass, BinaryColumnLength, ComplexTypeColumnLength,
    ConnectionTest, ConnectTimeout, IdPHost, IdPPort, LogLevel, LogPath, MaxCatalogNameLength, MaxColumnNameLength,
    MaxErrorRetry, MaxQueryExecutionPollingInterval, MaxSchemaNameLength, MaxStreamErrorRetry, MaxTableNameLength,
    MetadataRetrievalMethod, NonProxyHosts, Password, PWD, PreemptiveBasicProxyAuth, PreferredRole, Profile,
    ProxyDomain, ProxyHost, ProxyPort, ProxyPWD, ProxyUID, ProxyWorkstation, RowsToFetchPerBlock, S3OutputEncKMSKey,
    S3OutputEncOption, Schema, SocketTimeout, SSLInsecure, StringColumnLength, UseArraySupport, UseAwsLogger,
    User, UID, UseResultsetStreaming, Workgroup
  )

  private[this] val attributeNames = Seq(Url, Driver, ReadOnly, S3OutputLocation, S3OutputLocationPrefix) ++ optionalNames

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

  private[athena] lazy val readOnly: Option[Boolean] = map.get(ReadOnly).flatMap(v => Try(v.toBoolean).toOption)

  private[athena] lazy val options: Properties = {
    val p = new Properties()
    (map.get(S3OutputLocation), map.get(S3OutputLocationPrefix)) match {
      case (Some(d), Some(p)) => throw new ConfigException(s"duplicate settings: $prefix.$S3OutputLocation=$d, $prefix.$S3OutputLocationPrefix=$p")
      case (Some(v), _) => p.setProperty(S3OutputLocation, v)
      case (_, Some(v)) => p.setProperty(S3OutputLocation, s"$v/${UUID.randomUUID()}")
      case _ => throw new ConfigException(s"no configuration setting: key=$prefix.$S3OutputLocation, $prefix.$S3OutputLocationPrefix")
    }
    optionalNames.foreach { name =>
      map.get(name).foreach(value => p.setProperty(name, value))
    }
    p
  }

  private[athena] lazy val getTmpStagingDir: Option[String] = {
    if (map.contains(S3OutputLocationPrefix)) {
      Option(options.getProperty(S3OutputLocation))
    } else {
      None
    }
  }
}

object Config {
  val Url = "url"
  val Driver = "driver"
  val ReadOnly = "readOnly"

  val S3OutputLocation = "S3OutputLocation"
  val S3OutputLocationPrefix = "S3OutputLocationPrefix"

  val AwsCredentialsProviderArguments = "AwsCredentialsProviderArguments"
  val AwsCredentialsProviderClass = "AwsCredentialsProviderClass"
  val BinaryColumnLength = "BinaryColumnLength"
  val ComplexTypeColumnLength = "ComplexTypeColumnLength"
  val ConnectionTest = "ConnectionTest"
  val ConnectTimeout = "ConnectTimeout"
  val IdPHost = "IdP_Host"
  val IdPPort = "IdP_Port"
  val LogLevel = "LogLevel"
  val LogPath = "LogPath"
  val MaxCatalogNameLength = "MaxCatalogNameLength"
  val MaxColumnNameLength = "MaxColumnNameLength"
  val MaxErrorRetry = "MaxErrorRetry"
  val MaxQueryExecutionPollingInterval = "MaxQueryExecutionPollingInterval"
  val MaxSchemaNameLength = "MaxSchemaNameLength"
  val MaxStreamErrorRetry = "MaxStreamErrorRetry"
  val MaxTableNameLength = "MaxTableNameLength"
  val MetadataRetrievalMethod = "MetadataRetrievalMethod"
  val NonProxyHosts = "NonProxyHosts"
  val Password = "Password"
  val PWD = "PWD"
  val PreemptiveBasicProxyAuth = "PreemptiveBasicProxyAuth"
  val PreferredRole = "preferred_role"
  val Profile = "Profile"
  val ProxyDomain = "ProxyDomain"
  val ProxyHost = "ProxyHost"
  val ProxyPort = "ProxyPort"
  val ProxyPWD = "ProxyPWD"
  val ProxyUID = "ProxyUID"
  val ProxyWorkstation = "ProxyWorkstation"
  val RowsToFetchPerBlock = "RowsToFetchPerBlock"
  val S3OutputEncKMSKey = "S3OutputEncKMSKey"
  val S3OutputEncOption = "S3OutputEncOption"
  val Schema = "Schema"
  val SocketTimeout = "SocketTimeout"
  val SSLInsecure = "SSL_Insecure"
  val StringColumnLength = "StringColumnLength"
  val UseArraySupport = "UseArraySupport"
  val UseAwsLogger = "UseAwsLogger"
  val User = "User"
  val UID = "UID"
  val UseResultsetStreaming = "UseResultsetStreaming"
  val Workgroup = "Workgroup"
}

class ConfigException(message: String) extends Exception(message)
