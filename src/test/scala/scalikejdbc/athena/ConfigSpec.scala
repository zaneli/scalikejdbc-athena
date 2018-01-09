package scalikejdbc.athena

import org.scalatest.{FunSpec, OptionValues}

class ConfigSpec extends FunSpec with OptionValues {

  describe("Config") {
    it("default db") {
      val config = new Config("default")
      assert(config.url === "jdbc:awsathena://athena.us-east-2.amazonaws.com:443")
      assert(config.options.getProperty("s3_staging_dir") === "s3://query-results-bucket/folder/")
      assert(config.options.getProperty("log_path") === "logs/application.log")
      assert(config.getTmpStagingDir.isEmpty)
    }
    it("named db") {
      val config = new Config('athena)
      assert(config.url === "jdbc:awsathena://athena.ap-southeast-1.amazonaws.com:443")
      assert(config.options.getProperty("s3_staging_dir") !== "s3://query-results-bucket/folder")
      assert(config.options.getProperty("s3_staging_dir").startsWith("s3://query-results-bucket/folder") === true)
      assert(config.options.containsKey("log_path") === false)
      assert(config.getTmpStagingDir.value === config.options.getProperty("s3_staging_dir"))
    }
    it("invalid settings") {
      val config = new Config('duplicated)
      assertThrows[ConfigException](config.options)
    }
    it("unconfigured db") {
      assertThrows[ConfigException](new Config('unconfigured))
    }
    it("unsupported db name type") {
      assertThrows[ConfigException](new Config("athena".toCharArray))
    }
  }
}
