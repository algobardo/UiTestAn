import com.microsoft.z3
import com.microsoft.z3.{Sort, Context, Fixedpoint}

/**
 * Created by mezzetti on 09/12/14.
 */
class smallExample {

  def fun() {
    var ctx = new Context()
    var fp = ctx.mkFixedpoint()

    var B = ctx.mkBoolSort()

    var a = ctx.mkFuncDecl("a", new Array[Sort](0), B)
    var b = ctx.mkFuncDecl("b", new Array[Sort](0), B)
    var c = ctx.mkFuncDecl("c", new Array[Sort](0), B)

    fp.registerRelation(a)
    fp.registerRelation(b)
    fp.registerRelation(c)


    var one = ctx.mkImplies(ctx.mkConst(a).asInstanceOf[z3.BoolExpr], ctx.mkConst(b).asInstanceOf[z3.BoolExpr])
    var two = ctx.mkImplies(ctx.mkConst(b).asInstanceOf[z3.BoolExpr], ctx.mkConst(c).asInstanceOf[z3.BoolExpr])
    var three = ctx.mkConst(a).asInstanceOf[z3.BoolExpr]

    fp.addRule(one, ctx.mkSymbol("one"))
    fp.addRule(two, ctx.mkSymbol("two"))
    fp.addRule(three, ctx.mkSymbol("three"))

    println(fp.query(ctx.mkConst(a).asInstanceOf[z3.BoolExpr]))
  }
}
