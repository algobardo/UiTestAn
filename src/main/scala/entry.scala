import java.io.{FilenameFilter, File}
import java.net.{URL, URLClassLoader}

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.{IDevice, AndroidDebugBridge}
import soot.jimple.{Jimple, JimpleBody}
import soot.{Unit => SUnit, _}
import soot.options.Options
import java.util
import scala.collection.JavaConversions._



object entry {

  val pathIn = new File("./res/examples")
  val pathOut = new File("./out/")

  val appLocation = new File("/Volumes/Android4.4.3/androidtestingproject/Applications/Catroid-latest")
  val testSource = new File(appLocation, "/catroidTest/src")
  val testLocation = new File(appLocation, "build/intermediates/classes/test/debug")
  val mainLocation = new File(appLocation, "build/intermediates/classes/debug/")

  def main(args: Array[String]):Unit = {
    //var testClasses = instrument("org.catrobat")
    //execute(testClasses)
//    ParserExperiment.test(testSource)
  }

  def execute(testClasses: List[String]) = {

    val testFolder = new File("./out")
    val outJar = new File("./out/testClasses.jar")

    /*We take class names and all */
    val allClasses = Utils.listAllFiles(testFolder, new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = {return dir.getName.endsWith(".class")}
    })

    val allClassName = allClasses.map(f => f.getAbsolutePath).map(s => s.replace(testFolder.getAbsolutePath+"/","").replace(".class","").replace("/","."))

    /* First we zip the class files */
    Utils.zip(outJar, allClasses, testFolder)
    println("Zipped")

    val cl = URLClassLoader.newInstance(Array(new URL("file://" + outJar.getAbsolutePath)))
    println("Class loader initiated")
//
//    var instr = cl.loadClass("android.test.ActivityInstrumentationTestCase2")
//
//    println("ActivityInstrumentationTestCase loaded " + instr + " modifiers " + java.lang.reflect.Modifier.toString(instr.getModifiers))
    allClassName.foreach { s =>
      try {
        println("Loading class " + s)
        val c = cl.loadClass(s)
        println("Loaded class " + c)
      }
      catch {
        case e : Error => println("Error loading class " + s + e)
        case e: Exception => println("Exception loading class " + s + e)
      }
    }

    testClasses.foreach { s =>
      try {
        println("Getting class " + s)
        val c = Class.forName(s, true, cl)
        println("Got class " + c)
        val obj = c.newInstance()
        println("Instantiated class " + c + " into " + obj)
        val mtds = c.getMethods
        mtds.foreach { m =>
          if (m.getParameterTypes.length == 0 && m.getName.startsWith("test")) {
            m.invoke(obj)
          }
        }
      }
      catch {
        case e: Error => println("Error invoking "  + s + e)
        case e: Exception => println("Exception invoking " + s + e)
      }
    }
  }

  def mockify(m:SootMethod) = {
    println("Mockifying " + m + " phantom:" + m.isPhantom)
    m.setPhantom(false)
    val b = Jimple.v().newBody(m)
    b.insertIdentityStmts()
    m.setActiveBody(b)
  }

  def instrument(subpackage: String) : List[String] = {
    G.reset()
    //    Options.v().set_verbose(true);
    Options.v().set_src_prec(Options.src_prec_class)
    Options.v().set_output_format(Options.output_format_jimple)

    Options.v().set_allow_phantom_refs(true)
    Options.v().set_keep_line_number(true)
//    Options.v().set_whole_program(true)

    val procDir = new util.ArrayList[String]()
    procDir.add(pathIn.getAbsolutePath())

    Options.v().set_process_dir(procDir)
    Options.v().set_output_dir(pathOut.getAbsolutePath)


    //Options.v().set_soot_classpath(additionalClasspath)

    //    Options.v().set_include_all(true)

    // Needed for custom entry-points
//    val c = Scene.v().forceResolve("testExample", SootClass.BODIES)
//    c.setApplicationClass

    Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES)
    Scene.v().loadNecessaryClasses()
    Scene.v().loadBasicClasses()



    // Needed for custom entry-points
//    val method = c.getMethodByName("testSmth")
//    val entryPoints = new util.ArrayList[SootMethod]()
//    entryPoints.add(method)
//    Scene.v().setEntryPoints(entryPoints)

    val objectClass = Scene.v().getSootClass("java.lang.Object")

    /* Clean the classes, if the superclass if phantom, remove it */
    val allClasses = Scene.v().getClasses
    allClasses.foreach { c =>
      if(c != objectClass && c.resolvingLevel() >= SootClass.BODIES && c.getSuperclass.isPhantom)
        c.setSuperclass(objectClass)
    }



    PackManager.v().getPack("jtp").add(new Transform("jtp.methodAdder", new MethodAdder()))

    PackManager.v().runPacks()


    val phantomClasses = Scene.v().getPhantomClasses.toArray(new Array[SootClass](Scene.v().getPhantomClasses.size()))
    //
    phantomClasses.foreach { c =>
      //      c.setResolvingLevel(SootClass.BODIES)
      c.setModifiers(Modifier.PUBLIC)
      c.setApplicationClass()
      c.setSuperclass(objectClass)
      //      c.getMethods.foreach { m =>
      ////        mockify(m)
      //      }
    }


    val testClasses = Scene.v().getClasses.filter(c => !c.isPhantom).map(c => c.getName).filter(s => s.contains(subpackage))


    //    println("Call graph size " + Scene.v().getCallGraph.getClass)
//    println("body:" + Scene.v().getSootClass("android.app.Activity").getMethodByName("<init>").getActiveBody)



    if(!Options.v().oaat())
      PackManager.v().writeOutput()

    return testClasses.toList

  }

  def initAdb() = {
    AndroidDebugBridge.addDeviceChangeListener( new IDeviceChangeListener {
      def deviceConnected(device: IDevice) {
        System.out.println("* " + device.getSerialNumber)

      }
      def deviceDisconnected(device: IDevice): Unit = {

      }
      def deviceChanged(device: IDevice, changeMask: Int) {
      }
    })

    val adb: AndroidDebugBridge = AndroidDebugBridge.createBridge

    Thread.sleep(1000)
    if (!adb.isConnected) {
      System.out.println("Couldn't connect to ADB server")
    }

    AndroidDebugBridge.disconnectBridge

  }

}
