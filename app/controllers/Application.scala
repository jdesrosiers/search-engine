package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import search.QueryEngine

class Application extends Controller {

  implicit val searchInformationWrites = new Writes[QueryEngine.SearchInformation] {
    def writes(searchInformation: QueryEngine.SearchInformation) = Json.obj(
        "searchTime" -> searchInformation.searchTime,
        "totalResults" -> searchInformation.totalResults
      )
  }

  implicit val webPageWrites = new Writes[QueryEngine.WebPage] {
    def writes(webPage: QueryEngine.WebPage) = Json.obj(
        "link" -> webPage.link,
        "title" -> webPage.title,
        "score" -> webPage.score
      )
  }

  implicit val searchResultsWrites = new Writes[QueryEngine.SearchResults] {
    def writes(searchResults: QueryEngine.SearchResults) = Json.obj(
        "searchInformation" -> searchResults.searchInformation,
        "items" -> searchResults.items
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
