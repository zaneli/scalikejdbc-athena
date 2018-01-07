package scalikejdbc.athena

import java.sql.{Connection, DriverManager}

import scalikejdbc._

import scala.collection.breakOut
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

class AthenaSession(config: Config) extends DBSession {

  override private[scalikejdbc] lazy val conn: Connection = DriverManager.getConnection(config.url, config.options)

  override def collection[A, C[_]](
    template: String, params: Any*)(
    extract: WrappedResultSet => A)(
    implicit cbf: CanBuildFrom[Nothing, A, C[A]]
  ): C[A] = {
    using(conn.createStatement()) { stmt =>
      new ResultSetTraversable(stmt.executeQuery(StatementBuilder.build(template, params))).map(extract)(breakOut)
    }
  }

  override def single[A](template: String, params: Any*)(extract: WrappedResultSet => A): Option[A] = {
    using(conn.createStatement()) { stmt =>
      val resultSet = new ResultSetTraversable(stmt.executeQuery(StatementBuilder.build(template, params)))
      val rows = resultSet.map(rs => extract(rs)).toList
      if (rows.length > 1) {
        throw TooManyRowsException(1, rows.length)
      }
      rows.headOption
    }
  }

  override private[scalikejdbc] val connectionAttributes: DBConnectionAttributes = null
  override val tx: Option[Tx] = None
  override val isReadOnly: Boolean = true
  override protected[scalikejdbc] def settings: SettingsProvider = SettingsProvider.default
}
