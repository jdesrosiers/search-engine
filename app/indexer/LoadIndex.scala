package indexer.load

import akka.actor._
import akka.routing._
import indexer.Utilities._
import indexer.Indexer.Posting
import scala.io.Source

object Index {

  case class Result(result: Map[String, List[Posting]])

  var index: Map[String, List[Posting]] = Map()

}

class Index(filename: String) extends Actor {

  val reducer = context.actorOf(Props(new Reducer(self)))
  val deserializer = context.actorOf(SmallestMailboxPool(20).props(Props(new Deserializer(reducer))))

  context.watch(deserializer)
  context.watch(reducer)

  Source.fromFile(filename).getLines.foreach(deserializer ! Deserializer.Document(_))
  deserializer ! Broadcast(PoisonPill)

  def receive = {
    case Terminated(`deserializer`) => reducer ! PoisonPill
    case Terminated(`reducer`) =>
      context.stop(self)
      context.system.shutdown
    case Index.Result(result) =>
      Index.index = result
      result.take(10).foreach(println)
  }

}

object Deserializer {

  case class Document(json: String)

}

class Deserializer(reducer: ActorRef) extends Actor {

  def receive = {
    case Deserializer.Document(json) =>
      val doc = mapper.readValue[Map[String,Any]](json)
      val Some(token: String) = doc.get("token")
      val Some(postings: List[Map[String, Any]]) = doc.get("postings")

      reducer ! Reducer.Row(token, postings.map { case posting =>
        Posting(posting("url").asInstanceOf[String], posting("positions").asInstanceOf[List[Int]], posting("normalization").asInstanceOf[Double])
      })
  }

}

object Reducer {

  case class Row(token: String, postings: List[Posting])

}

class Reducer(main: ActorRef) extends Actor {

  var result = Map[String, List[Posting]]()

  def receive = {
    case Reducer.Row(token, postings) => result = result + (token -> postings)
  }

  override def postStop(): Unit = main ! Index.Result(result)

}

