package com.bidding

import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

case class Site(
  id: Int, 
  domain: String
)

object Server extends App {
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
        complete("Hello Bidder!") 
      }
    }
  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)
}
