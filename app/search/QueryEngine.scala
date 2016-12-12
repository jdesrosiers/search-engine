package search

import akka.actor._
import indexer.load.Index
import indexer.Utilities._
import scala.io.Source
import scalaz._
import scalaz.outlaws.std.double._
import Scalaz._

import scala.concurrent.duration._

object QueryEngine {

  case class WebPage(link: String, title: String, score: Double)
  case class SearchInformation(searchTime: Double, totalResults: Int)
  case class SearchResults(searchInformation: SearchInformation, items: List[WebPage])

  val sec = 1000000000.0

  val totalDocuments = Source.fromFile("totalDocuments").mkString.toInt

  val twoGramIndexSystem = ActorSystem()
  twoGramIndexSystem.actorOf(Props(new Index("twoGramIndex")))
  twoGramIndexSystem.awaitTermination(30 seconds)
  val twoGramIndex = Index.index

  val indexSystem = ActorSystem()
  indexSystem.actorOf(Props(new Index("index")))
  indexSystem.awaitTermination(30 seconds)
  val index = Index.index

  def search(query: String) = {
    val t0 = System.nanoTime()
    val results = cosineScore(query)
    val t1 = System.nanoTime()

    SearchResults(SearchInformation((t1 - t0) / sec, results.size), results.take(10))
  }

  def cosineScore(text: String) = {
    var scores = Map[String, Double]()
    var length = Map[String, Double]()

    frequencies(tokenize(Source.fromString(text)))
      .foreach { case (token, tokenFrequency) =>
        val postings = index.getOrElse(token, Nil)
        val idf = inverseDocumentFrequency(totalDocuments, postings.size)
        val queryWeight = termFrequency(tokenFrequency) * idf
        postings.foreach { case posting =>
            val docWeight = termFrequency(posting.positions.size) * idf
            val weight = docWeight * queryWeight
            length += (posting.url -> posting.normalization)
            scores += (posting.url -> (2 * scores.getOrElse(posting.url, 0.0) + weight))
          }
      }

    frequencies(nGrams(2, Source.fromString(text)))
      .foreach { case (token, tokenFrequency) =>
        val postings = twoGramIndex.getOrElse(token, Nil)
        val idf = inverseDocumentFrequency(totalDocuments, postings.size)
        val queryWeight = termFrequency(tokenFrequency) * idf
        postings.foreach { case posting =>
            val docWeight = termFrequency(posting.positions.size) * idf
            val weight = 1.65 * docWeight * queryWeight
            length += (posting.url -> posting.normalization)
            scores += (posting.url -> (2 * scores.getOrElse(posting.url, 0.0) + weight))
          }
      }

    scores
      .map { case (url, score) => WebPage(url, url, score / length(url)) }
      .toList.sortWith(_.score > _.score)
  }

  def termFrequency(tf: Int) = tf
  def inverseDocumentFrequency(n: Int, df: Double) = math.log(n / df)

}
