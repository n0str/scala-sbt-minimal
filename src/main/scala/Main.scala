import scalaj.http._
import net.liftweb.json._
import scala.xml.XML.loadString
import org.jsoup.Jsoup

import concurrent.ExecutionContext
import concurrent.Future
import concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class articleData(title: String, content: String, link: String)

/**
  * Created by Nostr on 2016/04/01.
  */
object Main{

  val feedUrl = "http://l-api.livejournal.com/__api/?callback=jQuery48656003971459680542829homepage__get_rating&request=%7B%22jsonrpc%22%3A%222.0%22%2C%22method%22%3A%22homepage.get_rating%22%2C%22params%22%3A%7B%22country%22%3A%22cyr%22%2C%22category_id%22%3A14%2C%22page%22%3A0%2C%22pagesize%22%3A50%7D%2C%22id%22%3A4865600397%7D"

  def main(args: Array[String]) {

    val response: HttpResponse[String] = Http(this.feedUrl).asString

    if (response.code != 200) {
      println(response.headers)
    }
    else {
      val responseBody = response.body
      val newStartIndex = responseBody.indexOf('(') + 1
      val newJsonLenght = responseBody.length() - 1

      val jsonText = response.body.substring(newStartIndex, newJsonLenght)
      val jsonAst = parse(jsonText)

      //    For extracting String from JString in JSON result
      implicit val formats = net.liftweb.json.DefaultFormats

      val newsList = (jsonAst \ "result" \ "rating").children
      val tasks: Seq[Future[Option[articleData]]] = for (newsElement <- newsList.take(3)) yield Future {
        val postUrl: String = (newsElement \ "post_url").extract[String]
        val postBody: String = (newsElement \ "body").extract[String]

        this.parseArticle(postUrl)
      }

      val aggregated: Future[Seq[Option[articleData]]] = Future.sequence(tasks)
      Await.result(aggregated, 15.seconds)

      println(aggregated)
    }

  }

  def parseArticle(articleLink: String): Option[articleData] = {

    val response: HttpResponse[String] = Http(articleLink).asString
    if (response.code != 200) {
      println(articleLink, " load error")
      return None
    }
    else {
      val html = Jsoup.parse(response.body)
      val title = html.select("title").first().text()

      val article = new articleData(title = title, content = "", link = "")
      return Some(article)
    }
    
  }

}