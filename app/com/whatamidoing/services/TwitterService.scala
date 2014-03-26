package com.whatamidoing.services

import com.whatamidoing.utils.ActorUtilsReader
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class TwitterService() {

       def getTwitterReferers(streamId: String): List[Tuple2[Float,Float]] = {
       	val linkedInReferers = ActorUtilsReader.getReferersForTwitter(streamId)
        var url = "https://freegeoip.net/json/"
	var referersLinkedin = List[(Float,Float)]()
	linkedInReferers.foreach {
	  case ip: String => { 

	         val mult = ip.split(",")
		  mult.foreach {op =>
		   val webserviceCall = url +op
                   import scala.concurrent._
		   import scala.concurrent.duration._
		   import play.api.libs.ws._

		  
		   val res = WS.url(webserviceCall).get().map {
		      response =>((response.json \"latitude").as[Float],(response.json \"longitude").as[Float])
                   }

		   val result = Await.result(res,5 seconds)
		   referersLinkedin = referersLinkedin ::: List(result)
                  }
           }
 
	 }
         referersLinkedin
       }

       def getTwitterCount(token: String): Tuple3[String,String,String] =  {
       //Getting info about linkedin
       val clause = ""
       val twitterInvites = ActorUtilsReader.countAllTwitterInvites(token,clause).head.toInt
       var res = ("","","")
       if (twitterInvites > 0) {      
          val twitterAccept = ActorUtilsReader.getTwitterAcceptanceCount(token,clause).head.toInt
          if (twitterAccept > 0) {
	   val accept = "("+twitterAccept+")"
	   res = ("Twitter","number watched",accept)
          } else {
	     res  = ("Twitter","","")

          }
       }  

        return res;
       }      


}


object TwitterService {
  def apply(): TwitterService = new TwitterService()
}