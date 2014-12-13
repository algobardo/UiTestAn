import java.io.{FileOutputStream, FilenameFilter, File, FileFilter}
import java.util.zip.ZipFile

/**
 * Created by mezzetti on 11/12/14.
 */
object Utils {

  def listAllFiles(file: File, filter: FilenameFilter): List[File] = {
    var files = file.listFiles()
    files.foldLeft(List[File]())( (l,f) => {
      if(f.isDirectory)
        listAllFiles(f,filter) ::: l
      else if (!filter.accept(f, f.getName))
        l
      else
        f :: l
    })
  }

  def copyFile(src: File, dest: File) {
    import java.io.{File,FileInputStream,FileOutputStream}
    new FileOutputStream(dest) getChannel() transferFrom(
      new FileInputStream(src) getChannel, 0, Long.MaxValue )
  }

  def zip(out: File, files: Iterable[File], basePath: File) = {
    import java.io.{BufferedInputStream, FileInputStream, FileOutputStream}
    import java.util.zip.{ZipEntry, ZipOutputStream}
//    println("base path is " + basePath)
    def relative(path: File) = {
      (basePath.toURI().relativize(path.toURI())).getPath
    }

    val zip = new ZipOutputStream(new FileOutputStream(out))

    files.foreach { name =>
//      println("zipping " + name + " relativepath " + relative(name))
      zip.putNextEntry(new ZipEntry(relative(name)))
      val in = new BufferedInputStream(new FileInputStream(name))
      var b = in.read()
      while (b > -1) {
        zip.write(b)
        b = in.read()
      }
      in.close()
      zip.closeEntry()
    }
    zip.close()
  }

  def unzip(in: File, out: File) = {

    val zipFile = new ZipFile(in)
    val enm = zipFile.entries()
    while (enm.hasMoreElements) {
      val entry = enm.nextElement()
      val filePath = new File(out, entry.getName);
      println("writing " + out + " -- " + entry.getName)
      if(entry.isDirectory)
        filePath.mkdirs()
      else {
        filePath.getParentFile.mkdirs()
        filePath.createNewFile()
        val fileout = new FileOutputStream(filePath)
        val unzipedFileIn = zipFile.getInputStream(entry)

        val buf = new Array[Byte](1024)
        var len = 0
        while ( {
          len = unzipedFileIn.read(buf)
          len != -1
        })
          fileout.write(buf, 0, len)
        fileout.close()
      }
    }
  }

}
