package com.whatamidoing.services

import com.whatamidoing.utils.ActorUtilsReader
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class FacebookService() {

       def getFacebookReferers(streamId: String): List[Tuple2[Float,Float]] = {
       	val facebookReferers = ActorUtilsReader.getReferersForFacebook(streamId)
        var url = "https://freegeoip.net/json/"
	var referersFacebook = List[(Float,Float)]()
	facebookReferers.foreach {
	  case ip: String => { 
		  val webserviceCall = url +ip
                  import scala.concurrent._
		  import scala.concurrent.duration._
		  import play.api.libs.ws._
		  val res = WS.url(webserviceCall).get().map {
		      response =>((response.json \"latitude").as[Float],(response.json \"longitude").as[Float])
                  }

		  val result = Await.result(res,5 seconds)
		 referersFacebook = referersFacebook ::: List(result)
           }
 
	 }
         referersFacebook
       }

       def getFacebookCount(token: String): Tuple3[String,String,String] =  {
       //Getting info about linkedin
       val clause = ""
       val facebookInvites = ActorUtilsReader.countAllFacebookInvites(token,clause).head.toInt
       var res = ("","","")
       if (facebookInvites > 0) {      
          val facebookAccept = ActorUtilsReader.getFacebookAcceptanceCount(token,clause).head.toInt
          if (facebookAccept > 0) {
	   val accept = "("+facebookAccept+")"
	   res = ("Facebook","number watched",accept)
          } else {
	     res  = ("Facebook","","")

          }
       }  
       return res;
       }      


}


object FacebookService {
  def apply(): FacebookService = new FacebookService()
}