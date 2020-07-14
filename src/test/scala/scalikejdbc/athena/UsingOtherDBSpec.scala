package scalikejdbc.athena

import java.sql.DriverManager

import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import scalikejdbc._

class UsingOtherDBSpec extends AnyFunSpec with BeforeAndAfter {
  // call this method to prevent stopping test on the way, but i don't know reason why...
  DriverManager.getDrivers

  before {
    NamedDB(User.connectionPoolName).athena { implicit s =>
      sql"""create table user (id int, name varchar(10))""".execute().apply()
    }
  }

  after {
    NamedDB(User.connectionPoolName).athena { implicit s =>
      sql"""drop table user""".execute().apply()
    }
  }

  describe("use h2db") {
    val users = Seq(User(1, "zaneli"), User(2, "za'ne'li"), User(3, "za?ne?li"))

    it("use SQLInterpolation") {
      val results = NamedDB(User.connectionPoolName).athena { implicit s =>

        val params = sqls.csv(users.map(u => sqls"(${u.id}, ${u.name})"): _*)
        val count = sql"""insert into user values $params""".executeUpdate().apply()
        assert(count === users.size)

        sql"""select id, name from user order by id""".map(r => User(r.int("id"), r.string("name"))).list().apply()
      }
      assert(users === results)
    }
    it("use QueryDSL") {
      val results = NamedDB(User.connectionPoolName).athena { implicit s =>
        val count = users.map { user =>
          withSQL { insert.into(User).values(user.id, user.name) }.update().apply()
        }.sum
        assert(count === users.size)

        val u = User.syntax("u")
        withSQL { select.from(User as u).orderBy(u.id) }.map(User(u.resultName)).list().apply()
      }
      assert(users === results)
    }
  }

  case class User(id: Long, name: String)
  object User extends SQLSyntaxSupport[User] {
    override lazy val connectionPoolName = 'h2
    override lazy val columnNames = Seq("id", "name")
    def apply(n: ResultName[User])(rs: WrappedResultSet): User = autoConstruct(rs, n)
  }
}
