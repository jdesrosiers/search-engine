package search

object QueryEngine {

  case class WebPage(url: String, title: String)
  case class SearchResults(totalDocuments: Int, searchTime: Double, topResults: List[WebPage])

  def search(query: String) = {
    val t0 = System.nanoTime()
    val results = List(WebPage("http://www.ics.uci.edu/", "Donald Bren School of Information and Computer Sciences @ University of California, Irvine"))
    val t1 = System.nanoTime()

    val sec = 1000000000.0
    SearchResults(results.size, (t1 - t0) / sec, results.take(10))
  }

}
