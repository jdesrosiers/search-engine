package indexer

import akka.actor._
import akka.routing._
import scala.io.Source
import Utilities._

object Indexer {

  type Index = Map[String, List[Posting]];

  case class Postings(postings: Map[String, Posting])
  case class Posting(url: String, positions: List[Int], normalization: Double)
  case class Result(indexType: String, index: Index)

}

class IndexerMain extends Actor {
  val reducer = context.actorOf(Props(new Reducer("index", self)))
  val tokenizer = context.actorOf(SmallestMailboxPool(20).props(Props(new Tokenizer(reducer))))

  context.watch(tokenizer)
  context.watch(reducer)

  val t0 = System.nanoTime()

  var totalDocuments = 0
  Source.fromFile("allPages").getLines.foreach {
    case line =>
      totalDocuments = totalDocuments + 1
      tokenizer ! line
  }
  tokenizer ! Broadcast(PoisonPill)

  def receive = {
    case Terminated(`tokenizer`) => reducer ! PoisonPill
    case Terminated(`reducer`) => context.stop(self)
    case Indexer.Result(indexType, index) =>
      println(s"Total Documents: $totalDocuments")
      println(s"Unique Words: ${index.size}")

      val t1 = System.nanoTime()
      println("Execution Time: " + ((t1 - t0) / 1000000000.0) + "sec")

      // memory info
      val mb = 1024*1024
      val runtime = Runtime.getRuntime
      println("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory) / mb)
      println("** Free Memory:  " + runtime.freeMemory / mb)
      println("** Total Memory: " + runtime.totalMemory / mb)
      println("** Max Memory:   " + runtime.maxMemory / mb)

      index.take(10).foreach(println)

      mapper.writeValue(new java.io.File("totalDocuments"), totalDocuments)
      index
        //.take(10)
        .foreach { case (token, postings) =>
          val out = new java.io.StringWriter
          mapper.writeValue(out, Map("token" -> token, "postings" -> postings))
          println(out.toString)
        }
  }
}

class Tokenizer(reducer: ActorRef) extends Actor {
  def receive = {
    case line: String =>
      val doc = mapper.readValue[Map[String,Any]](line)
      val Some(url: String) = doc.get("_id")
      val Some(text: String) = doc.get("text")
      val anchorText = doc.get("anchorText") match {
        case Some(anchorText: List[String]) => anchorText
        case _ => Nil
      }

      val fullText = (text :: anchorText).mkString(" ")

      val normalization = frequencies(tokenize(Source.fromString(fullText))) match {
        case freq if (freq.isEmpty) => 0.0
        case freq => math.sqrt(freq.map(_._2.asInstanceOf[Double]).reduce(_ + math.pow(_, 2)))
      }

      //reducer ! Indexer.Postings(index(url, tokenize(Source.fromString(fullText)), normalization))
      reducer ! Indexer.Postings(index(url, nGrams(2, Source.fromString(fullText)), normalization))
  }

  def index(url: String, tokens: Iterator[(String, Int)], normalization: Double) = {
    tokens.foldLeft(Map[String, Indexer.Posting]()) { case (acc, (token, position)) =>
        val existing = acc.getOrElse(token, Indexer.Posting(url, Nil, normalization))
        acc + (token -> Indexer.Posting(url, position :: existing.positions, normalization))
      }
  }
}

class Reducer(indexType: String, main: ActorRef) extends Actor {
  var index = Map[String, List[Indexer.Posting]]()

  def receive = {
    case Indexer.Postings(postings) =>
      index = postings.foldLeft(index) { case (sum, (token, posting)) =>
        sum + (token -> (posting :: sum.getOrElse(token, Nil)))
      }
  }

  override def postStop(): Unit = main ! Indexer.Result(indexType, index)
}

