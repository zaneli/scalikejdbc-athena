package scalikejdbc.athena

import java.sql.{Connection, DriverManager}
import java.util.TimeZone

import scalikejdbc.{DBConnectionAttributes, DBSession, SettingsProvider, TimeZoneSettings, Tx}

class AthenaSession(config: Config) extends DBSession {

  override private[scalikejdbc] val conn: Connection = {
    val c = new AthenaConnection(DriverManager.getConnection(config.url, config.options))
    config.readOnly.foreach(c.setReadOnly)
    c
  }

  def getTmpStagingDir: Option[String] = config.getTmpStagingDir

  override private[scalikejdbc] val connectionAttributes: DBConnectionAttributes = {
    val timeZoneSettings = config.timeZone.fold(TimeZoneSettings()) { timeZone =>
      TimeZoneSettings(conversionEnabled = true, serverTimeZone = TimeZone.getTimeZone(timeZone))
    }
    DBConnectionAttributes(driverName = Some(config.driver), timeZoneSettings = timeZoneSettings)
  }

  override val tx: Option[Tx] = None
  override val isReadOnly: Boolean = conn.isReadOnly
  override protected[scalikejdbc] def settings: SettingsProvider = SettingsProvider.default
}
