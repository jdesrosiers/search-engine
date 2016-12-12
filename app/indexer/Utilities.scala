package indexer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import scala.io.Source

object Utilities {
  implicit val codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.REPLACE)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  val stopWordsFile = "src/main/resources/stopwords"
  lazy val stopWords = Source.fromFile(stopWordsFile).getLines.map(_.replaceAll("'", "")).toSet

  def tokenize(source: Source): Iterator[(String, Int)] = source.getLines
    .map(_.replaceAll("""(?<=\w)[',](?=\w)""", ""))
    .map(_.replaceAll("""(?<=\d)[.](?=\d)""", "__TOKENZ__DECML__"))
    .flatMap(_.split("""\W+"""))
    .filterNot(_ == "")
    .map(_.replaceAll("__TOKENZ__DECML__", "."))
    .map(_.toLowerCase)
    .zipWithIndex
    .filterNot { case (token, index) => !token.matches(""".*[a-z].*""") && token.size > 4 }
    .filterNot { case (token, index) => token.size < 3 || token.size > 20 }
    .filterNot { case (token, index) => stopWords(token) }

  def nGrams(n: Integer, source: Source): Iterator[(String, Int)] = tokenize(source)
    .sliding(n)
    .filter { case item => item.last._2 - item.head._2 == n - 1 }
    .filter(_.length == n)
    .map(_.unzip)
    .map { case (tokens, pos :: _) => (tokens.mkString(" "), pos)  }

  def frequencies(tokens: Iterator[(String, Int)]): Iterator[(String, Int)] = tokens
    .foldLeft(Map[String, Int]()) { case (acc, (token, _)) =>
      acc + (token -> (acc.getOrElse(token, 0) + 1))
    }
    .toIterator

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }
}

