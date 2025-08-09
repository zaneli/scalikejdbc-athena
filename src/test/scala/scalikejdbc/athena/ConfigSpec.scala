package scalikejdbc.athena

import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpec

class ConfigSpec extends AnyFunSpec with OptionValues {

  describe("Config") {
    it("default db") {
      val config = new Config("default")
      assert(config.url === "jdbc:athena://AwsRegion=us-east-2")
      assert(config.options.getProperty("S3OutputLocation") === "s3://query-results-bucket/folder/")
      assert(config.options.getProperty("LogPath") === "logs/application.log")
      assert(config.getTmpStagingDir.isEmpty)
      assert(config.readOnly.value === false)
      assert(config.timeZone.isEmpty)
    }
    it("named db") {
      val config = new Config("athena")
      assert(config.url === "jdbc:athena://AwsRegion=ap-southeast-1")
      assert(config.options.getProperty("S3OutputLocation") !== "s3://query-results-bucket/folder")
      assert(config.options.getProperty("S3OutputLocation").startsWith("s3://query-results-bucket/folder") === true)
      assert(config.options.containsKey("LogPath") === false)
      assert(config.getTmpStagingDir.value === config.options.getProperty("S3OutputLocation"))
      assert(config.readOnly.value === true)
      assert(config.timeZone.value === "UTC")
    }
    it("invalid settings") {
      val config = new Config("duplicated")
      assertThrows[ConfigException](config.options)
      assert(config.readOnly.isEmpty)
    }
    it("unconfigured db") {
      assertThrows[ConfigException](new Config("unconfigured"))
    }
    it("unsupported db name type") {
      assertThrows[ConfigException](new Config("athena".toCharArray))
    }
  }
}
