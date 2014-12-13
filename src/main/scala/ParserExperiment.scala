import java.io.{FilenameFilter, File}

import japa.parser.JavaParser
import japa.parser.ast.visitor.{CloneVisitor, GenericVisitorAdapter}

/**
 * Created by mezzetti on 12/12/14.
 */
object ParserExperiment {

  def test(srcLocation: File): Unit = {
    val allSourceFiles = Utils.listAllFiles(srcLocation, new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = {
        return dir.getName.contains(".java")
      }
    })
    val l = new Localizer()
    allSourceFiles.foreach { f =>
      val p = JavaParser.parse(f)
      p.accept(l,null)
    }
  }

}
