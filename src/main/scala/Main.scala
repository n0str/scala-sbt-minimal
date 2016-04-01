import scalaj.http._

/**
  * Created by Nostr on 2016/04/01.
  */
object Main {

  def main(args: Array[String]) {
    println("Hello world")
    val response: HttpResponse[String] = Http("https://ya.ru/").asString
    println(response.body)
  }

}
