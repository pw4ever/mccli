package mccli.response

import scala.collection.mutable.ListBuffer
import argonaut._
import Argonaut._
import ArgonautScalaz._

import scalaz.PLensFamily


object ParseResponse {
  case class ArtifactInfo(
                           id: String,
                           group: String,
                           artifact: String,
                           latestVersion: String,
                           repositoryId: String,
                           packaging: String,
                           timestamp: Long,
                           versionCount: Long,
                           text: List[String],
                           ec: List[String],
                         )
  implicit private def ArtifactInfoDecodeJson: DecodeJson[ArtifactInfo] =
    jdecode10L(ArtifactInfo.apply)(
      "id", "g", "a", "latestVersion", "repositoryId",
      "p", "timestamp", "versionCount", "text", "ec"
    )


  case class ResponseResult(
                             numFound: Long,
                             artifacts: Seq[ArtifactInfo]
                           )

  def apply(jsonStr: String): ResponseResult = {
    var numFound = 0L
    val buf = ListBuffer.empty[ArtifactInfo]
    for (json <- Parse.parseOption(jsonStr)) {
      val responseLens = jObjectPL >=> jsonObjectPL("response") >=> jObjectPL

      val numFoundLens = responseLens >=> jsonObjectPL("numFound") >=> jNumberPL
      numFound = numFoundLens.getOr(json, JsonLong(0L)).toLong.getOrElse(0L)

      val docsLens = responseLens >=> jsonObjectPL("docs") >=> jArrayPL
      docsLens.get(json).foreach(docs => docs.foreach(
        doc =>
          doc.as[ArtifactInfo].value.foreach( _ +=: buf)
      ))
    }
    ResponseResult(
      numFound = numFound,
      artifacts = buf.toList.reverse
    )
  }

}
