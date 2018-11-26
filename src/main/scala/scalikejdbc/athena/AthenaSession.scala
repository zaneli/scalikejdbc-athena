package scalikejdbc.athena

import java.sql.{Connection, DriverManager}

import scalikejdbc.{DBConnectionAttributes, DBSession, SettingsProvider, Tx}

class AthenaSession(config: Config) extends DBSession {

  override private[scalikejdbc] lazy val conn: Connection = {
    val c = new AthenaConnection(DriverManager.getConnection(config.url, config.options))
    config.readOnly.foreach(c.setReadOnly)
    c
  }

  def getTmpStagingDir: Option[String] = config.getTmpStagingDir

  override private[scalikejdbc] val connectionAttributes: DBConnectionAttributes = null
  override val tx: Option[Tx] = None
  override val isReadOnly: Boolean = conn.isReadOnly
  override protected[scalikejdbc] def settings: SettingsProvider = SettingsProvider.default
}
