package mccli.parseopts

import scala.language.reflectiveCalls

import mccli.globals._
import org.rogach.scallop._

// https://github.com/scallop/scallop#quick-example
class ParseOpts private[parseopts](args: Seq[String]) extends ScallopConf(args) {
  version(s"${Globals.name} ${Globals.version} by ${Globals.maintainer}")

  banner(
    s"""
       |Maven Central Command-line Interface (mccli) is a tool to query
       |the Maven Central repository.
    """.stripMargin)

  val rows = opt[Long](name = "rows", short = 'r', descr = "Number of rows to include in response")
  val raw = toggle(name = "raw", short = 'R', descrYes = "Dump raw JSON response for CLI pipeline processing")
  val verbosity = tally(name = "verbose", short = 'v', descr = "Increase verbosity")

  // coordinate search is the default
  val group = opt[String](name = "group", short = 'g', descr = "Maven POM coordinate group ID")
  val artifact = opt[String](name = "artifact", short = 'a', descr = "Maven POM coordinate artifact ID")
  val version = opt[String](name = "version", short = 'V', descr = "Maven POM coordinate version")
  val packaging = opt[String](name = "packaging", short = 'p', descr = "Maven artifact packaging")
  val classifier = opt[String](name = "classifier", short = 'l', descr = "Maven artifact classifier")

  val klass = new Subcommand("class") {
    val className = opt[String](name = "class", short = 'c', descr = "Class name")
    val fullClassName = opt[String](name = "fullclass", short = 'f', descr = "Full class name")
  }
  addSubcommand(klass)

  val checksum = new Subcommand("checksum") {
    val sha1 = opt[String](name = "sha1", short = '1', descr = "SHA-1 checksum")
  }
  addSubcommand(checksum)

  val rawQuery = trailArg[List[String]](required = false, descr = "Raw query string to send")

  verify()
}

/**
  * CLI argument parsing result.
  * @param queryString Query string.
  * @param rows Number of rows to include in the response.
  * @param raw Dump raw JSON response, instead of tabulated result.
  */
case class ParseOptsResult(queryString: String, rows: Long, raw: Boolean, verbosity: Int)

object ParseOpts {
  /**
    * Parse command line arguments.
    * @param args The command line argument.
    * @return CLI argument parsing result.
    */
  def apply(args: Seq[String]): ParseOptsResult = {
    val conf = new ParseOpts(args)

    val mcPrefix = Map(
      conf.artifact -> "a:",
      conf.group -> "g:",
      conf.version -> "v:",
      conf.packaging -> "p:",
      conf.classifier -> "l:",

      conf.klass.className -> "c:",
      conf.klass.fullClassName -> "fc:",

      conf.checksum.sha1 -> "1:"
    )

    def concatPrefixedParams(params: ScallopOption[String]*): String =
      params.filter(_.isSupplied).map(param => mcPrefix(param) + param()).mkString(sep = " ").trim

    val queryString: String = conf.subcommand match {
      case Some(conf.klass) =>
        concatPrefixedParams(
          conf.klass.className,
          conf.klass.fullClassName,
        )
      case Some(conf.checksum) =>
        concatPrefixedParams(
          conf.checksum.sha1,
        )
      case _ =>
        (concatPrefixedParams(
          conf.artifact,
          conf.group,
          conf.version,
          conf.packaging,
          conf.classifier,
        ) + conf.rawQuery.getOrElse(List()).mkString(" ")).trim
    }

    if (queryString.trim.isEmpty) { // Show help if not querying.
      conf.printHelp()
    }

    ParseOptsResult(
      queryString = queryString,
      rows = conf.rows.getOrElse(10).max(1),
      raw = conf.raw.getOrElse(false),
      verbosity = conf.verbosity(),
    )
  }
}
