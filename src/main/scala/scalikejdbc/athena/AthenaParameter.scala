package scalikejdbc.athena

sealed abstract class AthenaParameter[A](quote: Boolean, str: A => String = { (a: A) => a.toString }) {
  protected[this] def _value: A

  def value: String = {
    Option(_value).fold("null") { v =>
      if (quote) {
        s"'${str(v).replace("'", "''")}'"
      } else {
        str(v)
      }
    }
  }
}

object AthenaParameter {
  import java.time.ZonedDateTime
  import java.time.format.DateTimeFormatter

  private[this] val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSS")

  case class Int(_value: scala.Int) extends AthenaParameter[scala.Int](false)
  case class BigInt(_value: scala.Long) extends AthenaParameter[scala.Long](false)
  case class Boolean(_value: scala.Boolean) extends AthenaParameter[scala.Boolean](false)
  case class Double(_value: scala.Double) extends AthenaParameter[scala.Double](false)
  case class String(_value: java.lang.String) extends AthenaParameter[java.lang.String](true)
  case class Timestamp(_value: ZonedDateTime) extends AthenaParameter[ZonedDateTime](true, formatter.format)
  case object Null extends AthenaParameter[Any](false, _ => "null") {
    override protected[this] def _value: Any = null
  }
}
