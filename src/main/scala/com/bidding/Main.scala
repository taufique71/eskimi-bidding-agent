package com.bidding

import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

case class Site(
  id: Int, 
  domain: String
)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val siteFormat = jsonFormat2(Site)
}

object Server extends App with JsonSupport {
  implicit val system = ActorSystem("actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val routes = 
    path("") { 
      get {
        complete("Hello World!") 
      }
    } ~
    path("bid") { 
      post {
        entity(as[Site]) { site =>
          println(site.id, site.domain)
          complete("Hello Bidder!") 
        }
      }
    }
  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)
}
