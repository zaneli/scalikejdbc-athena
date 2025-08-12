package scalikejdbc.athena

import java.sql.{Connection, DriverManager}
import java.util.TimeZone

import scalikejdbc.{DBConnectionAttributes, DBSession, SettingsProvider, TimeZoneSettings, Tx}

class AthenaSession(config: Config) extends DBSession {

  override private[scalikejdbc] val conn: Connection = {
    val original = DriverManager.getConnection(config.url, config.options)
    val connection = if (config.useCustomPreparedStatement) {
      // In older versions of the Athena JDBC driver, PreparedStatement was not supported,
      // so we used a custom implementation as a workaround.
      // This is generally unnecessary for Athena JDBC driver 2.x or later.
      new AthenaConnection(original)
    } else {
      original
    }
    config.readOnly.foreach(connection.setReadOnly)
    connection
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
