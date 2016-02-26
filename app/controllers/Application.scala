package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import search.QueryEngine

class Application extends Controller {

  implicit val webPageWrites = new Writes[QueryEngine.WebPage] {
    def writes(webPage: QueryEngine.WebPage) = Json.obj(
        "url" -> webPage.url,
        "title" -> webPage.title
      )
  }

  implicit val searchResultsWrites = new Writes[QueryEngine.SearchResults] {
    def writes(searchResults: QueryEngine.SearchResults) = Json.obj(
        "totalDocuments" -> searchResults.totalDocuments,
        "searchTime" -> searchResults.searchTime,
        "topResults" -> searchResults.topResults
      )
  }

  def index = Action {
    Ok(views.html.index())
  }

  def search = Action { implicit request =>
    val Some(query) = request.getQueryString("query")
    val results = QueryEngine.search(query)

    render {
      case Accepts.Json() => Ok(Json.toJson(results))
    }
  }

}
