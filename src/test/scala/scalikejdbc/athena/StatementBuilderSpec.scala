package scalikejdbc.athena

import org.scalatest.funspec.AnyFunSpec

class StatementBuilderSpec extends AnyFunSpec {

  describe("StatementBuilder") {
    it("no param") {
      val template = "select * from test_tbl limit 10;"
      val result = StatementBuilder.build(template, Nil)
      assert(result === template)
    }
    it("some params") {
      val template = "select * from test_tbl where id = ? and name = ? limit 10;"
      val result = StatementBuilder.build(template, Seq(AthenaParameter.Int(10), AthenaParameter.String("zaneli")))
      assert(result === "select * from test_tbl where id = 10 and name = 'zaneli' limit 10;")
    }
    it("some params contains `'`") {
      val template = "select * from test_tbl where id = ? and name = ? limit 10;"
      val result = StatementBuilder.build(template, Seq(AthenaParameter.Int(10), AthenaParameter.String("zaneli' or name != 'zaneli")))
      assert(result === "select * from test_tbl where id = 10 and name = 'zaneli'' or name != ''zaneli' limit 10;")
    }
    it("template contains `?`") {
      val template = "select * from test_tbl where id = ? and name in ('?', ?) limit 10;"
      val result = StatementBuilder.build(template, Seq(AthenaParameter.Int(10), AthenaParameter.String("zaneli")))
      assert(result === "select * from test_tbl where id = 10 and name in ('?', 'zaneli') limit 10;")
    }
    it("invalid placeholder and param size") {
      val template = "select * from test_tbl limit 10;"
      assertThrows[IllegalStateException](StatementBuilder.build(template, Seq(AthenaParameter.Int(10))))
    }
  }
}
