import japa.parser.ast.Node
import japa.parser.ast.expr.MethodCallExpr
import japa.parser.ast.visitor.CloneVisitor

/**
 * Created by mezzetti on 13/12/14.
 */
class Localizer extends CloneVisitor {

  override def visit(_n: MethodCallExpr, _arg: Object): Node = {
    println(_n.getName + " name_ " + _n.getNameExpr + " scope:" + _n.getScope)

    return super.visit(_n, _arg)
  }
}
