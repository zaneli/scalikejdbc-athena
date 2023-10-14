package scalikejdbc.athena

import java.sql.DriverManager
import java.time.{ZoneId, ZonedDateTime}

import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import scalikejdbc._

class UsingOtherDBSpec extends AnyFunSpec with BeforeAndAfter {
  // call this method to prevent stopping test on the way, but i don't know reason why...
  DriverManager.getDrivers

  before {
    NamedDB(User.connectionPoolName).athena { implicit s =>
      sql"""create table users (id int, name varchar(10), created_at timestamp)""".execute.apply()
    }
  }

  after {
    NamedDB(User.connectionPoolName).athena { implicit s =>
      sql"""drop table users""".execute.apply()
    }
  }

  describe("use h2db") {
    val time1 = ZonedDateTime.of(2020,8,13,10,20,30,0, ZoneId.of("Asia/Tokyo"))
    val time2 = ZonedDateTime.of(2020,8,13,11,20,30,0, ZoneId.of("Asia/Tokyo"))
    val time3 = ZonedDateTime.of(2020,8,13,12,20,30,0, ZoneId.of("Asia/Tokyo"))
    val users = Seq(User(1, "zaneli", time1), User(2, "za'ne'li", time2), User(3, "za?ne?li", time3))

    it("use SQLInterpolation") {
      val results = NamedDB(User.connectionPoolName).athena { implicit s =>

        val params = sqls.csv(users.map(u => sqls"(${u.id}, ${u.name}, ${u.createdAt})"): _*)
        val count = sql"""insert into users values $params""".executeUpdate.apply()
        assert(count === users.size)

        sql"""select id, name, created_at from users order by id""".map(r => User(r.int("id"), r.string("name"), r.zonedDateTime("created_at"))).list.apply()
      }
      assert(users === results)
    }
    it("use QueryDSL") {
      val results = NamedDB(User.connectionPoolName).athena { implicit s =>
        val count = users.map { user =>
          withSQL { insert.into(User).values(user.id, user.name, user.createdAt) }.update.apply()
        }.sum
        assert(count === users.size)

        val u = User.syntax("u")
        withSQL { select.from(User as u).orderBy(u.id) }.map(User(u.resultName)).list.apply()
      }
      assert(users === results)
    }
  }

  case class User(id: Long, name: String, createdAt: ZonedDateTime)
  object User extends SQLSyntaxSupport[User] {
    override lazy val connectionPoolName = "h2"
    override lazy val tableName = "users"
    override lazy val columnNames = Seq("id", "name", "created_at")
    def apply(n: ResultName[User])(rs: WrappedResultSet): User = autoConstruct(rs, n)
  }
}
