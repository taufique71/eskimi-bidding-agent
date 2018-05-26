package com.bidding

import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

case class Site( id: Int, domain: String )
case class Geo( country: Option[String], city: Option[String], lat: Option[Double], lon: Option[Double] )
case class Impression( id: String, wmin: Option[Int], wmax: Option[Int], w: Option[Int],
  hmin: Option[Int], hmax: Option[Int], h: Option[Int], bidFloor: Option[Double]
)
case class User(id: String, geo: Option[Geo])
case class Device(id: String, geo: Option[Geo])
case class BidRequest(id: String, impression: Option[List[Impression]], site: Site, user: Option[User], device: Option[Device])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val siteFormat = jsonFormat2(Site)
  implicit val geoFormat = jsonFormat4(Geo)
  implicit val impressionFormat = jsonFormat8(Impression)
  implicit val userFormat = jsonFormat2(User)
  implicit val deviceFormat = jsonFormat2(Device)
  implicit val bidRequestFormat = jsonFormat5(BidRequest)
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
        entity(as[BidRequest]) { bidRequest =>
          println(bidRequest)
          //user.geo match {
            //case Some(g) => println(g)
            //case None => println("User has no geolocation")
          //}
          complete("Hello Bidder!") 
        }
      }
    }
  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)
}
