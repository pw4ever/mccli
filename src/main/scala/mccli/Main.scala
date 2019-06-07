package mccli

import mccli.parseopts.{ParseOpts, ParseOptsResult}
import mccli.response.ParseResponse

import com.softwaremill.sttp._

object Main extends App {
  val parsedOpts: ParseOptsResult = ParseOpts(args)
  if (!parsedOpts.queryString.trim().isEmpty) {
    val uri = uri"https://search.maven.org/solrsearch/select?q=${parsedOpts.queryString}&rows=${parsedOpts.rows}&wt=json"
    val request = sttp.get(uri)

    if (parsedOpts.verbosity > 0) println(request.uri)

    implicit val backend = HttpURLConnectionBackend()
    val response = request.send()

    response.body match {

      case Right(res) => // 2xx: Success
        if (parsedOpts.raw)
          println(res)
        else {
          val parsedResponse = ParseResponse(res)
          if (parsedOpts.verbosity > 0) println(parsedResponse.numFound)
          if (parsedResponse.numFound > 0) {
            for (artifact <- parsedResponse.artifacts) {
              val output = Seq(
                artifact.id,
                artifact.latestVersion,
                java.time.Instant.ofEpochMilli(artifact.timestamp).toString,
                artifact.packaging,
                artifact.versionCount.toString,
              ).mkString(sep = "\t")
              println(output)
            }
          }
        }

      case Left(error) => // !2xx: Error
        Console.err.println(error)
    }
  }
}
