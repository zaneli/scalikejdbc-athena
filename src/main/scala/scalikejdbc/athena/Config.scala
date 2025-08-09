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

  private[this] val map = if (config.hasPath(prefix)) {
    config.getConfig(prefix).entrySet.asScala.map { entry =>
      val key = entry.getKey
      key -> config.getString(s"$prefix.$key")
    }.toMap
  } else {
    throw new ConfigException(s"no configuration setting: key=$prefix")
  }

  map.get(Driver).foreach(Class.forName)

  private[athena] lazy val url: String = map.getOrElse(Url, throw new ConfigException(s"no configuration setting: key=$prefix.$Url"))
  private[athena] lazy val driver: String = map.getOrElse(Driver, throw new ConfigException(s"no configuration setting: key=$prefix.$Driver"))
  private[athena] lazy val readOnly: Option[Boolean] = map.get(ReadOnly).flatMap(v => Try(v.toBoolean).toOption)
  private[athena] lazy val timeZone: Option[String] = map.get(TimeZone)

  private[athena] lazy val options: Properties = {
    val p = new Properties()
    (map.get(S3OutputLocation), map.get(S3OutputLocationPrefix)) match {
      case (Some(d), Some(p)) => throw new ConfigException(s"duplicate settings: $prefix.$S3OutputLocation=$d, $prefix.$S3OutputLocationPrefix=$p")
      case (Some(v), _) => p.setProperty(S3OutputLocation, v)
      case (_, Some(v)) => p.setProperty(S3OutputLocation, s"$v/${UUID.randomUUID()}")
      case _ => throw new ConfigException(s"no configuration setting: key=$prefix.$S3OutputLocation, $prefix.$S3OutputLocationPrefix")
    }
    map.foreach { entry =>
      val (name, value) = entry
      p.setProperty(name, value)
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
  val TimeZone = "timeZone"

  val S3OutputLocation = "S3OutputLocation"
  val S3OutputLocationPrefix = "S3OutputLocationPrefix"
}

class ConfigException(message: String) extends Exception(message)
