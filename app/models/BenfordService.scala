package models

object BenfordService {

  case class srvData(filePath: String)
  case class srvCalc(numberSamples: Int)
  case class srvCIsByGroupId(groupId: Int)
  case class srvBenfordCIsByGroupId(groupId: Int)
  case class srvResultsByGroupId(groupId: Int)
  case class srvFrequenciesByGroupId(groupId: Int)
  case class srvGroups()
  case class srvNumSamples()
  case class srvTestsByGroupId(groupId: Int)

}
