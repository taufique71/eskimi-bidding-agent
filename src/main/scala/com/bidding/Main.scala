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
case class Impression( id: String, bidFloor: Option[Double],
  wmin: Option[Int], wmax: Option[Int], w: Option[Int],
  hmin: Option[Int], hmax: Option[Int], h: Option[Int]
)
case class User( id: String, geo: Option[Geo] )
case class Device( id: String, geo: Option[Geo] )
case class BidRequest( id: String, impression: Option[List[Impression]], site: Site, user: Option[User], device: Option[Device] )
case class TimeRange( timeStart: Long, timeEnd: Long )
case class Targeting( cities: List[String], targetedSiteIds: List[Int] )
case class Banner( id: Int, src: String, width: Int, height: Int)
case class Campaign( id: Int, userId: Int, country: String, bid: Double,
  runningTimes: Set[TimeRange], targeting: Targeting, banners: List[Banner]
)
case class BidResponse( id: String, bidRequestId: String, price: Double, adid: Option[String], banner: Option[Banner])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val siteFormat = jsonFormat2(Site)
  implicit val geoFormat = jsonFormat4(Geo)
  implicit val impressionFormat = jsonFormat8(Impression)
  implicit val userFormat = jsonFormat2(User)
  implicit val deviceFormat = jsonFormat2(Device)
  implicit val bidRequestFormat = jsonFormat5(BidRequest)
  implicit val timeRangeFormat = jsonFormat2(TimeRange)
  implicit val targetingFormat = jsonFormat2(Targeting)
  implicit val bannerFormat = jsonFormat4(Banner)
  implicit val campaignFormat = jsonFormat7(Campaign)
  implicit val bidResponseFormat = jsonFormat5(BidResponse)
}

object Server extends App with JsonSupport {
  implicit val system = ActorSystem("actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  //val campaigns: List[Campaign] = List(
    //Campaign()  
  //)

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
