package com.bidding

import akka.actor.ActorSystem
import scala.concurrent.Future
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import scala.util.control._

case class Site( id: Int, domain: String )
case class Geo( country: Option[String], city: Option[String], lat: Option[Double], lon: Option[Double] )
case class Impression( id: String, bidFloor: Option[Double],
  wmin: Option[Int], wmax: Option[Int], w: Option[Int],
  hmin: Option[Int], hmax: Option[Int], h: Option[Int]
)
case class User( id: String, geo: Option[Geo] )
case class Device( id: String, geo: Option[Geo] )
case class BidRequest( id: String, imp: Option[List[Impression]], site: Site, 
  user: Option[User], device: Option[Device] )
case class TimeRange( timeStart: Long, timeEnd: Long )
case class Targeting( cities: List[String], targetedSiteIds: Set[Int] )
case class Banner( id: Int, src: String, width: Int, height: Int)
case class Campaign( id: Int, userId: Int, country: String,
  runningTimes: Set[TimeRange], targeting: Targeting, banners: List[Banner], bid: Double
)
case class BidResponse( id: Int, bidRequestId: String, price: Double, adid: Option[String], banner: Option[Banner])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val siteFormat = jsonFormat2(Site)
  implicit val geoFormat = jsonFormat4(Geo)
  implicit val impressionFormat = jsonFormat8(Impression)
  implicit val userFormat = jsonFormat2(User)
  implicit val deviceFormat = jsonFormat2(Device)
  implicit def bidRequestFormat = jsonFormat5(BidRequest)
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
  val r = scala.util.Random

  val campaignPool: List[Campaign] = List(
    Campaign( 
      123, 
      213, 
      "Bangladesh", 
      Set(TimeRange(1600, 1700), TimeRange(2030, 2230)), 
      Targeting(List("Dhaka", "Chuadanga"),Set(122429, 104797)), 
      List(Banner(321, "BDFlag", 10, 6), Banner(32, "Random Bangladeshi Banner", 15, 14)),
      10.34
    ), 
    Campaign( 
      124, 
      214, 
      "India", 
      Set(TimeRange(1300, 1415), TimeRange(1840, 2000)), 
      Targeting(List("Mumbai", "Bengalore"),Set(31, 104798)), 
      List(Banner(321, "IndiaFlag", 10, 6), Banner(32, "Random Indian Banner", 15, 14)),
      1.6
    ), 
    Campaign( 
      125, 
      215, 
      "Some Country", 
      Set(TimeRange(1300, 1415), TimeRange(1840, 2000)), 
      Targeting(List("City A", "City B"),Set(122430, 104798)), 
      List(Banner(321, "SomeFlag", 10, 6), Banner(32, "Some Random Banner", 15, 14)),
      1.6
    ) 
  )

  val routes = 
    path("") { 
      get {
        complete("Hello World!") 
      }
    } ~
    path("bid") { 
      post {
        entity(as[BidRequest]) { bidRequest =>
          var bidResponse: Option[BidResponse] = None
          for(campaign <- campaignPool){
            if(campaign.targeting.targetedSiteIds(bidRequest.site.id)){
              if(bidRequest.imp.isDefined){
                val impressions = bidRequest.imp.get
                for(imp <- impressions){
                  for(banner <- campaign.banners){
                    val wmin = imp.wmin.getOrElse(0)
                    val wmax = imp.wmax.getOrElse(1000) // Assuming that wmax can be 1000 units 
                    val hmin = imp.hmin.getOrElse(0)
                    val hmax = imp.hmax.getOrElse(1000) // Assuming that hmax can be 1000 units 
                    
                    var flag = true
                    
                    if(imp.w.isDefined && imp.w.get != banner.width) flag = false
                    else if(imp.h.isDefined && imp.h.get != banner.height) flag = false
                    else if(banner.width > wmax) flag = false
                    else if(banner.width < wmin) flag = false
                    else if(banner.height > hmax) flag = false
                    else if(banner.height < hmin) flag = false
                    if(flag){
                      bidResponse = Some(BidResponse(r.nextInt(1000), bidRequest.id, campaign.bid, None, Some(banner)))
                    }
                  }
                }
              }
              else{
                bidResponse = Some(BidResponse(r.nextInt(1000), bidRequest.id, campaign.bid, None, None))
              }
            }
          }
          bidResponse match {
            case Some(r) => complete(r)
            case None => complete(StatusCodes.NoContent)
          }
        }
      }
    }
  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)
}
