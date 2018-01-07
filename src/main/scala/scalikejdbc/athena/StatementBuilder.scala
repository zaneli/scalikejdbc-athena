package scalikejdbc.athena

import scalikejdbc._

import scala.annotation.tailrec

object StatementBuilder {

  def build(template: String, params: Seq[Any]): String = {
    var i = 0
    var isInsideOfText = false
    template.map {
      case c@'\'' =>
        isInsideOfText = !isInsideOfText
        c
      case '?' if !isInsideOfText =>
        val replaced = normalize(params(i))
        i += 1
        replaced
      case c =>
        c
    }.mkString
  }

  private[this] def normalize(param: Any): String = {
    @tailrec
    def norm(param: Any): Any = {
      param match {
        case ParameterBinder(v) => norm(v)
        case None => null
        case Some(p) => norm(p)
        case p: String => p
        case p: java.util.Date => p.toSqlTimestamp.toString
        case p => p
      }
    }

    norm(param) match {
      case null => "null"
      case s: String => s"'$s'"
      case p => p.toString
    }
  }
}
