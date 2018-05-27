name := "Eskimi Bidding Agent"

version := "0.1"

organization := "com.bidding"

scalaVersion := "2.12.2"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= {
  val AkkaVersion = "2.4.18"
  val AkkaHttpVersion = "10.0.6"
  Seq(
    "com.typesafe.akka"     %%      "akka-http"             % AkkaHttpVersion,
    "com.typesafe.akka"     %%      "akka-http-spray-json"  % AkkaHttpVersion,
  )
}
