package scalikejdbc.athena

import java.time.{ZoneId, ZonedDateTime}

import org.scalatest.funspec.AnyFunSpec

class AthenaParameterSpec extends AnyFunSpec {

  describe("AthenaParameter.Int") {
    it("pos") {
      val p = AthenaParameter.Int(10)
      assert(p.value === "10")
    }
    it("neg") {
      val p = AthenaParameter.Int(-1)
      assert(p.value === "-1")
    }
  }
  describe("AthenaParameter.BigInt") {
    it("pos") {
      val p = AthenaParameter.BigInt(10000000000L)
      assert(p.value === "10000000000")
    }
    it("neg") {
      val p = AthenaParameter.BigInt(-10000000000L)
      assert(p.value === "-10000000000")
    }
  }
  describe("AthenaParameter.Boolean") {
    it("true") {
      val p = AthenaParameter.Boolean(true)
      assert(p.value === "true")
    }
    it("false") {
      val p = AthenaParameter.Boolean(false)
      assert(p.value === "false")
    }
  }
  describe("AthenaParameter.Double") {
    it("pos") {
      val p = AthenaParameter.Double(10.5D)
      assert(p.value === "10.5")
    }
    it("neg") {
      val p = AthenaParameter.Double(-10.5D)
      assert(p.value === "-10.5")
    }
  }
  describe("AthenaParameter.String") {
    it("not null") {
      val p = AthenaParameter.String("value")
      assert(p.value === "'value'")
    }
    it("contains `'`") {
      val p = AthenaParameter.String("val'ue")
      assert(p.value === "'val''ue'")
    }
    it("null") {
      val p = AthenaParameter.String(null)
      assert(p.value === "null")
    }
  }
  describe("AthenaParameter.Timestamp") {
    it("not null") {
      val date = ZonedDateTime.of(2018, 1, 10, 2, 30, 50, 123000000, ZoneId.of("UTC"))
      val p = AthenaParameter.Timestamp(date)
      assert(p.value === "'2018-01-10 02:30:50.123'")
    }
    it("null") {
      val p = AthenaParameter.Timestamp(null)
      assert(p.value === "null")
    }
  }
  describe("AthenaParameter.Null") {
    it("null") {
      val p = AthenaParameter.Null
      assert(p.value === "null")
    }
  }
}
