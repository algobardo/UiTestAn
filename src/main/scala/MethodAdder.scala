import soot.jimple._
import soot.{Unit => SUnit, _}
import soot.jimple.internal.{JInterfaceInvokeExpr, JInvokeStmt}
import soot.tagkit.{Tag, Host}
import soot.util.Switch
import scala.collection.JavaConversions._

/**
 * Created by mezzetti on 10/12/14.
 */
class MethodAdder extends BodyTransformer {

  override def internalTransform(b: soot.Body, phaseName: String, options: java.util.Map[String, String]): scala.Unit = {

    val units = b.getUnits

    //important to use snapshotIterator here
    val it = units.snapshotIterator()
    while (it.hasNext) {
      val unit = it.next()
      val stmt = unit.asInstanceOf[Stmt]

      var invokeOffending = false

      if (stmt.containsInvokeExpr) {
        val invokeExpr = stmt.getInvokeExpr
        val declClass = invokeExpr.getMethod.getDeclaringClass
        if (declClass.isPhantom && !invokeExpr.isInstanceOf[SpecialInvokeExpr])
          invokeOffending = true
      }
      val fieldOffending = if (stmt.containsFieldRef()) {
        stmt.getFieldRef.getField.getDeclaringClass.isPhantom
      } else false
      val (leftFieldOffending, rightFieldOffending) = stmt match {
        case ass: AssignStmt =>
          (ass.getLeftOp match {
            case fr: FieldRef =>
              fr.getField.getDeclaringClass.isPhantom
            case _ => false
          },
            ass.getRightOp match {
              case fr: FieldRef =>
                fr.getField.getDeclaringClass.isPhantom
              case _ => false
            })
        case _ => (false, false)
      }

      stmt match {
        case ass: AssignStmt =>
          if (invokeOffending || rightFieldOffending) {
            val tp = Type.toMachineType(ass.getRightOp.getType)
            val cnst = tp match {
              case int: IntType => IntConstant.v(0)
              case ref: ArrayType => Jimple.v().newNewArrayExpr(ref, IntConstant.v(1))
              case ref: RefType => NullConstant.v()
              case float: FloatType => FloatConstant.v(0)
              case double: DoubleType => DoubleConstant.v(0)
              case long: LongType => LongConstant.v(0)
            }
            ass.setRightOp(cnst)
          }
          else if (leftFieldOffending)
            units.remove(unit)
        case _ =>
          if (fieldOffending || invokeOffending)
            units.remove(unit);
      }
    }

    //            val tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"))
    //            b.getLocals().add(tmpRef);
    //
    //            units.insertBefore(Jimple.v().newAssignStmt(tmpRef, Jimple.v().newStaticFieldRef(
    //              Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), cUnit)
    //
    //            val toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>")
    //            units.insertBefore(Jimple.v().newInvokeStmt
    //              (Jimple.v().newVirtualInvokeExpr
    //                (tmpRef, toCall.makeRef(), StringConstant.v(cUnit.toString))),cUnit);
  b.validate()
  }
}