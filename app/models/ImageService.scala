package models

object ImageService {

  case class srvDirect(data: java.io.ByteArrayOutputStream)
  case class srvData(filePath: String)
  case class srvCalc(windowSize: Int)
  case class srvImage()
  case class srvSBAImage(threshold: Double, whiteBackground: Boolean)

}
