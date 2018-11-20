package scalikejdbc.athena

import org.scalatest.{FunSpec, OptionValues}

class ConfigSpec extends FunSpec with OptionValues {

  describe("Config") {
    it("default db") {
      val config = new Config("default")
      assert(config.url === "jdbc:awsathena://AwsRegion=us-east-2")
      assert(config.options.getProperty("S3OutputLocation") === "s3://query-results-bucket/folder/")
      assert(config.options.getProperty("LogPath") === "logs/application.log")
      assert(config.getTmpStagingDir.isEmpty)
    }
    it("named db") {
      val config = new Config('athena)
      assert(config.url === "jdbc:awsathena://AwsRegion=ap-southeast-1")
      assert(config.options.getProperty("S3OutputLocation") !== "s3://query-results-bucket/folder")
      assert(config.options.getProperty("S3OutputLocation").startsWith("s3://query-results-bucket/folder") === true)
      assert(config.options.containsKey("LogPath") === false)
      assert(config.getTmpStagingDir.value === config.options.getProperty("S3OutputLocation"))
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
