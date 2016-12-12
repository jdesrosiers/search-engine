package indexer.anchorText

import akka.actor._
import akka.routing._
import org.jsoup.Jsoup
import collection.JavaConversions._
import scala.io.Source
import java.net.URL
import scalaz._
import Scalaz._

import indexer.Utilities._

object AnchorText {

  case class Result(result: Map[String, List[String]])

}

class AnchorText extends Actor {

  val reducer = context.actorOf(Props(new Reducer(self)))
  val extractor = context.actorOf(SmallestMailboxPool(10).props(Props(new Extractor(reducer))))
  val tokenizer = context.actorOf(SmallestMailboxPool(10).props(Props(new Tokenizer(extractor))))

  context.watch(tokenizer)
  context.watch(extractor)
  context.watch(reducer)

  Source.fromFile("allPages").getLines.foreach(tokenizer ! Tokenizer.Document(_))
  tokenizer ! Broadcast(PoisonPill)

  def receive = {
    case Terminated(`tokenizer`) => extractor ! Broadcast(PoisonPill)
    case Terminated(`extractor`) => reducer ! PoisonPill
    case Terminated(`reducer`) => context.stop(self)
    case AnchorText.Result(result) =>
      result
        //.take(10)
        .foreach { case (url, anchorText) =>
        val out = new java.io.StringWriter
        mapper.writeValue(out, Map("url" -> url, "anchorText" -> anchorText))
        println(out.toString)
      }
  }

}

object Tokenizer {

  case class Document(json: String)

}

class Tokenizer(extractor: ActorRef) extends Actor {

  def receive = {
    case Tokenizer.Document(json) =>
      val doc = mapper.readValue[Map[String,Any]](json)
      val Some(url: String) = doc.get("_id")
      val Some(content: String) = doc.get("content")

      extractor ! Extractor.Document(url, content)
  }

}

object Extractor {

  case class Document(url: String, content: String)

}

class Extractor(reducer: ActorRef) extends Actor {

  def receive = {
    case Extractor.Document(url, content) =>
      val context = new URL(url)
      val links = Jsoup.parse(content)
        .select("a[href]")
        .toList
        .map { case link =>
          try { new URL(context, link.attr("href")) -> Jsoup.parse(link.text).text() }
          catch { case _ : Throwable => (new URL("http://malformedurl") -> "") }
        }
        .filter { case (link, _) => link.getHost.matches("""(.*\.)?ics.uci.edu""") }
        .filterNot { case (_, text) => text.isEmpty }
        .map { case (url, anchorTexts) => (url.toString, anchorTexts) }
      reducer ! Reducer.Links(links)
  }

}

object Reducer {

  case class Links(links: List[(String, String)])
  case class AnchorText(anchorText: Map[String, List[String]])

}

class Reducer(main: ActorRef) extends Actor {

  var result = Map[String, List[String]]()

  def receive = {
    case Reducer.Links(links) => result = links.foldLeft(result) {
      case (acc, (url, anchorText)) =>
        val existing = acc.getOrElse(url, Nil)
        acc + (url -> (anchorText :: existing))
    }
  }

  override def postStop(): Unit = main ! AnchorText.Result(result)

}
