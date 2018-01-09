package scalikejdbc.athena

object StatementBuilder {

  def build(template: String, params: Seq[AthenaParameter[_]]): String = {
    var i = 0
    var isInsideOfText = false
    val sql = template.map {
      case c @ '\'' =>
        isInsideOfText = !isInsideOfText
        c
      case '?' if !isInsideOfText =>
        val v = params(i).value
        i += 1
        v
      case c =>
        c
    }.mkString
    if (i != params.size) {
      throw new IllegalStateException(s"unexpected placeholder and param size: template=$template, params=$params")
    }
    sql
  }
}
