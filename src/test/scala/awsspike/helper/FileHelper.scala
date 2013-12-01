package awsspike.helper

object FileHelper {

  def readFile(filepath: String): String = {
    io.Source.fromInputStream(getClass.getResourceAsStream(filepath)).mkString
  }

}
