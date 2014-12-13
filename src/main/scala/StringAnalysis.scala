import java.util

import soot.jimple._
import soot.toolkits.graph.{Orderer, DirectedGraph}
import soot.toolkits.graph.interaction.FlowInfo
import soot.toolkits.scalar.ForwardFlowAnalysis
import soot.{Unit => SUnit, _}
import scala.collection.JavaConversions._

import scala.collection.mutable

/**
 * Created by mezzetti on 10/12/14.
 */
class StringAnalysis(graph:DirectedGraph[SUnit], b: Body)
  extends ForwardFlowAnalysis[SUnit, StringDomain](graph:DirectedGraph[SUnit]) {
  
  override def flowThrough(in: StringDomain, n: SUnit, out: StringDomain): Unit = {
    var sw = new AbstractStmtSwitch {
      override def caseAssignStmt(stmt: AssignStmt): Unit = super.caseAssignStmt(stmt)

      override def caseBreakpointStmt(stmt: BreakpointStmt): Unit = super.caseBreakpointStmt(stmt)

      override def caseEnterMonitorStmt(stmt: EnterMonitorStmt): Unit = super.caseEnterMonitorStmt(stmt)

      override def caseExitMonitorStmt(stmt: ExitMonitorStmt): Unit = super.caseExitMonitorStmt(stmt)

      override def caseGotoStmt(stmt: GotoStmt): Unit = super.caseGotoStmt(stmt)

      override def caseIdentityStmt(stmt: IdentityStmt): Unit = super.caseIdentityStmt(stmt)

      override def caseIfStmt(stmt: IfStmt): Unit = super.caseIfStmt(stmt)

      override def caseInvokeStmt(stmt: InvokeStmt): Unit = super.caseInvokeStmt(stmt)

      override def caseLookupSwitchStmt(stmt: LookupSwitchStmt): Unit = super.caseLookupSwitchStmt(stmt)

      override def caseNopStmt(stmt: NopStmt): Unit = super.caseNopStmt(stmt)

      override def caseRetStmt(stmt: RetStmt): Unit = super.caseRetStmt(stmt)

      override def caseReturnStmt(stmt: ReturnStmt): Unit = super.caseReturnStmt(stmt)

      override def caseReturnVoidStmt(stmt: ReturnVoidStmt): Unit = super.caseReturnVoidStmt(stmt)

      override def caseTableSwitchStmt(stmt: TableSwitchStmt): Unit = super.caseTableSwitchStmt(stmt)

      override def caseThrowStmt(stmt: ThrowStmt): Unit = super.caseThrowStmt(stmt)
    }
  }

  override def copy(a: StringDomain, a1: StringDomain): Unit = {}

  override def merge(in1: StringDomain, in2: StringDomain, out: StringDomain): Unit = {
    in1.foreach{case (l,s)  =>
        out.put(l, out(l).union(s))
    }
    in2.foreach{case (l,s)  =>
      out.put(l, out(l).union(s))
    }

  }

  def getInitialFlow():StringDomain = {
    var init = new StringDomain()
    b.getLocals.iterator().foreach(l => init += (l -> new scala.collection.immutable.HashSet[String]()))
    return init
  }

  override def newInitialFlow(): StringDomain = {
    getInitialFlow()
  }

  override def entryInitialFlow(): StringDomain = {
    getInitialFlow()
  }
}
