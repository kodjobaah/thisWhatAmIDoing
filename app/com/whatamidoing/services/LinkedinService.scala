package com.whatamidoing.services

import com.whatamidoing.utils.ActorUtilsReader
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class LinkedinService() {

       def getLinkedInReferers(streamId: String): List[Tuple2[Float,Float]] = {
       	val linkedInReferers = ActorUtilsReader.getReferersForLinkedin(streamId)
        var url = "https://freegeoip.net/json/"
	var referersLinkedin = List[(Float,Float)]()
	linkedInReferers.foreach {
	  case ip: String => { 
		  val webserviceCall = url +ip
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
         referersLinkedin
       }

       def getLinkedInCount(token: String): Tuple3[String,String,String] =  {
       //Getting info about linkedin
       val clause = ""
       val linkedinInvites = ActorUtilsReader.countAllLinkedinInvites(token,clause).head.toInt
       var res = ("","","")
       if (linkedinInvites > 0) {      
          val linkedinAccept = ActorUtilsReader.getLinkedinAcceptanceCount(token,clause).head.toInt
          if (linkedinAccept > 0) {
	   val accept = "("+linkedinAccept+")"
	   res = ("LinkedIn","number watched",accept)
          } else {
	     res  = ("Linkedin","","")

          }
       }  
        return res;
       }      


}


object LinkedinService {
  def apply(): LinkedinService = new LinkedinService()
}