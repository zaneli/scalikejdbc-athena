package scalikejdbc

package object athena {

  private[this] def withAthena[A](dbName: Any)(execution: AthenaSession => A): A = {
    using(new AthenaSession(new Config(dbName))) { session =>
      execution(session)
    }
  }

  implicit class AthenaDB(db: DB.type) {
    def athena[A](execution: AthenaSession => A): A = {
      withAthena(ConnectionPool.DEFAULT_NAME)(execution)
    }
  }

  implicit class NamedAthenaDB(db: NamedDB) {
    def athena[A](execution: AthenaSession => A): A = {
      withAthena(db.name)(execution)
    }
  }
}
