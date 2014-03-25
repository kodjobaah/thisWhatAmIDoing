package com.whatamidoing.utils

import akka.actor.ActorSystem
import com.whatamidoing.cypher.CypherReaderFunction
import akka.pattern.ask
import akka.pattern.AskTimeoutException
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import com.whatamidoing.actors.neo4j.Neo4JReader
import com.whatamidoing.actors.red5.FrameSupervisor
import models.Location
import scala.concurrent.Future
import play.api.Logger

object ActorUtilsReader {

  val system = ActorSystem("whatamidoing-system")
  implicit val timeout = Timeout(500 seconds)
  var frameSupervisor = system.actorOf(FrameSupervisor.props("hey"), "frameSupervisor")
  var neo4jreader = system.actorOf(Neo4JReader.props(), "neo-4j-reader-supervisor")

  import models.Messages._

  def getUserToken(em: String) = {
    val getUserToken = CypherReaderFunction.getUserToken(em)
    //Used by ?(ask)

    val getUserTokenResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getUserToken)).mapTo[Any]
    var res = Await.result(getUserTokenResponse, 10 seconds) match {
      case ReadOperationResult(results) => {
        if (results.results.size > 0) {
          val tok = results.results.head.asInstanceOf[(String, String)]
          if (tok._2.equalsIgnoreCase("true") ){
            tok._1
          } else {
            "-1"
          }
        } else {
         "-1"
        }
      }
    }
    res
  }


  def searchForUser(em: String) = {

    val searchForUser = CypherReaderFunction.searchForUser(em)
    import com.whatamidoing.actors.neo4j.Neo4JReader._
    val response: Future[Any] = ask(neo4jreader, PerformReadOperation(searchForUser)).mapTo[Any]

    var res = Await.result(response, 10 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.mkString
      }
    }
    res
  }

  def streamNameForToken(token: String) = {
    val findStreamForToken = CypherReaderFunction.findActiveStreamForToken(token)
    val findStreamForTokenResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(findStreamForToken)).mapTo[Any]
    var streamName = Await.result(findStreamForTokenResponse, 10 seconds) match {
      case ReadOperationResult(readResults) => {

        if (readResults.results.size > 0) {
          readResults.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }

    streamName
  }

  def getValidToken(token: String) = {
    val getValidToken = CypherReaderFunction.getValidToken(token)
    import com.whatamidoing.actors.neo4j.Neo4JReader._
    val getValidTokenResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getValidToken)).mapTo[Any]


    var res = Await.result(getValidTokenResponse, 10 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results
      }
    }

    res
  }


  def findStreamForInvitedId(invitedId: String) = {
    val createInvite = CypherReaderFunction.findStreamForInvitedId(invitedId)
    val readerResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(createInvite)).mapTo[Any]

    var streamName = Await.result(readerResponse, 10 seconds) match {
      case ReadOperationResult(results) => {

        if (results.results.size > 0) {
          results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    streamName

  }


  def findStreamForInviteTwitter(invitedId: String) = {
    val inviteTwitter = CypherReaderFunction.findStreamForInviteTwitter(invitedId)
    val readerResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(inviteTwitter)).mapTo[Any]

    var streamName = Await.result(readerResponse, 10 seconds) match {
      case ReadOperationResult(results) => {

        if (results.results.size > 0) {
          results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    streamName

  }

  def findStreamForInviteFacebook(invitedId: String) = {
    val inviteFacebook = CypherReaderFunction.findStreamForInviteFacebook(invitedId)
    val readerResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(inviteFacebook)).mapTo[Any]

    var streamName = Await.result(readerResponse, 10 seconds) match {
      case ReadOperationResult(results) => {

        if (results.results.size > 0) {
          results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    streamName

  }


  def findStreamForInviteLinkedin(invitedId: String) = {
    val inviteLinkedin = CypherReaderFunction.findStreamForInviteLinkedin(invitedId)
    val readerResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(inviteLinkedin)).mapTo[Any]

    var streamName = Await.result(readerResponse, 10 seconds) match {
      case ReadOperationResult(results) => {

        if (results.results.size > 0) {
          results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    streamName

  }

  /*
   * NOTE: This is not currently being used
   */
  def checkToSeeIfTwitterInviteAcceptedAlreadyByReferer(inviteId: String, referer: String) = {
    val checkToSeeIfTwitterInviteAcceptedAlreadyByReferer = CypherReaderFunction.checkToSeeIfTwitterInviteAcceptedAlreadyByReferer(inviteId,referer)
    val readerResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(checkToSeeIfTwitterInviteAcceptedAlreadyByReferer)).mapTo[Any]

    var id = Await.result(readerResponse, 10 seconds) match {
      case ReadOperationResult(results) => {

        if (results.results.size > 0) {
          results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
   id

  }

  def findActiveStreamForToken(token: String): String = {
    
    val findStreamForToken = CypherReaderFunction.findActiveStreamForToken(token)
    import com.whatamidoing.actors.neo4j.Neo4JReader._
    val getValidTokenResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(findStreamForToken)).mapTo[Any]

    var streamName = Await.result(getValidTokenResponse, 10 seconds) match {
      case ReadOperationResult(readResults) => {
        
        if (readResults.results.size > 0 ) {
        	readResults.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    streamName
  }


   def  findAllInvites(email: String): List[String] = {
    
    val findAllInvites = CypherReaderFunction.findAllInvites(email)
    val findAllInvitesResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(findAllInvites)).mapTo[Any]

    var results = Await.result(findAllInvitesResponse, 10 seconds) match {
      case ReadOperationResult(readResults) => {
    	  readResults.results.asInstanceOf[List[String]]
      	}
      }
    results
  }

  def  findAllStreamsForDay(token: String, displayStart: Int, displayLength: Int, sortColumn: Int, sortDirection: String): List[String] = {

    val findAllInvites = CypherReaderFunction.findAllStreamsForDay(token,displayStart,displayLength,sortColumn,sortDirection)
    val findAllInvitesResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(findAllInvites)).mapTo[Any]

    var results = Await.result(findAllInvitesResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

  def  findAllInvitesForStream(token: String, displayStart: Int, displayLength: Int, sortColumn: Int, sortDirection: String, streamId: String): List[String] = {

    val findAllInvitesForStream = CypherReaderFunction.findAllInvitesForStream(token,displayStart,displayLength,sortColumn,sortDirection,streamId)
    val findAllInvitesForStreamResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(findAllInvitesForStream)).mapTo[Any]

    var results = Await.result(findAllInvitesForStreamResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

  def  getUsersWhoHaveAcceptedToWatchStream(token: String): List[String] = {

    val getUsersWhoHaveAcceptedToWatchStream = CypherReaderFunction.getUsersWhoHaveAcceptedToWatchStream(token)
    val getUsersWhoHaveAcceptedToWatchStreamResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getUsersWhoHaveAcceptedToWatchStream)).mapTo[Any]

    var results = Await.result(getUsersWhoHaveAcceptedToWatchStreamResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def  getUsersWhoHaveBeenInvitedToWatchStream(token: String): List[String] = {

    val getUsersWhoHaveBeenInvitedToWatchStream = CypherReaderFunction.getUsersWhoHaveBeenInvitedToWatchStream(token)
    val getUsersWhoHaveBeenInvitedToWatchStreamResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getUsersWhoHaveBeenInvitedToWatchStream)).mapTo[Any]

    var results = Await.result(getUsersWhoHaveBeenInvitedToWatchStreamResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def  getUsersWhoHaveAcceptedToWatchStreamUsingStreamId(streamId: String): List[String] = {

    val getUsersWhoHaveAcceptedToWatchStreamUsingStreamId = CypherReaderFunction.getUsersWhoHaveAcceptedToWatchStreamUsingStreamId(streamId)
    val getUsersWhoHaveAcceptedToWatchStreamUsingStreamIdResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getUsersWhoHaveAcceptedToWatchStreamUsingStreamId)).mapTo[Any]

    var results = Await.result(getUsersWhoHaveAcceptedToWatchStreamUsingStreamIdResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def  getUsersWhoHaveBeenInvitedToWatchStreamUsingStreamId(streamId: String): List[String] = {

    val getUsersWhoHaveBeenInvitedToWatchStreamUsingStreamId = CypherReaderFunction.getUsersWhoHaveBeenInvitedToWatchStreamUsingStreamId(streamId)
    val getUsersWhoHaveBeenInvitedToWatchStreamUsingStreamIdResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getUsersWhoHaveBeenInvitedToWatchStreamUsingStreamId)).mapTo[Any]

    var results = Await.result(getUsersWhoHaveBeenInvitedToWatchStreamUsingStreamIdResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def  countNumberAllStreamsForDay(token: String): List[String] = {

    val countNumberAllStreamsForDay = CypherReaderFunction.countNumberAllStreamsForDay(token)
    val countNumberAllStreamsForDayResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(countNumberAllStreamsForDay)).mapTo[Any]

    var results = Await.result(countNumberAllStreamsForDayResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }



  def  countAllInvitesForToken(token: String): List[String] = {

    val countAllInvitesForToken = CypherReaderFunction.countAllInvitesForToken(token)
    val countAllInvitesForTokenResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(countAllInvitesForToken)).mapTo[Any]

    var results = Await.result(countAllInvitesForTokenResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def  findAllTokensForUser(email: String): List[String] = {

    val findAllTokensForUser = CypherReaderFunction.findAllTokensForUser(email)
    val findAllTokensForUserResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(findAllTokensForUser)).mapTo[Any]

    var results = Await.result(findAllTokensForUserResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

  def  getStreamsForCalendar(email: String, startYear: Int, endYear: Int,
                             startMonth: Int, endMonth : Int,
                             startDay: Int, endDay: Int): List[String] = {

    val getStreamsForCalendar = CypherReaderFunction.getStreamsForCalendar(email,startYear,endYear,startMonth,endMonth,startDay, endDay)
    val getStreamsForCalendarResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getStreamsForCalendar)).mapTo[Any]

    var results = Await.result(getStreamsForCalendarResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def  getStreamsForCalendarThatHaveEnded(email: String, startYear: Int, endYear: Int,
                             startMonth: Int, endMonth : Int,
                             startDay: Int, endDay: Int): List[String] = {

    val getStreamsForCalendarThatHaveEnded = CypherReaderFunction.getStreamsForCalendarThatHaveEnded(email,startYear,endYear,startMonth,endMonth,startDay, endDay)
    val getStreamsForCalendarThatHaveEndedResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getStreamsForCalendarThatHaveEnded)).mapTo[Any]

    var results = Await.result(getStreamsForCalendarThatHaveEndedResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def getEmailUsingToken(token: String):String = {

    val getEmailUsingToken = CypherReaderFunction.getEmailUsingToken(token)
    val getEmailUsingTokenResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getEmailUsingToken)).mapTo[Any]

    val results = Await.result(getEmailUsingTokenResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result =""
             for(res <- readResults.results){

               var x: String = res match {
                  case Some(s:String) => s
                  case None => "?"
                }

               if (!x.equals("?")) {
                 result = x
               }
             }

            Logger.info("results "+result)
            result
      }
    }
   results
}


  def checkToSeeIfCheckPasswordIdIsValid(cpId: String):String = {

    val checkToSeeIfCheckPasswordIdIsValid = CypherReaderFunction.checkToSeeIfCheckPasswordIdIsValid(cpId)
    val checkToSeeIfCheckPasswordIdIsValidResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(checkToSeeIfCheckPasswordIdIsValid)).mapTo[Any]

    val results = Await.result(checkToSeeIfCheckPasswordIdIsValidResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result =""
             for(res <- readResults.results){

               var x: String = res match {
                  case Some(s:String) => s
                  case None => "?"
                }

               if (!x.equals("?")) {
                 result = x
               }
             }

            Logger.info("results "+result)
            result
      }
    }
   results
  }


  import models.UserDetails
  def fetchUserDetails(token: String): UserDetails = {

    val fetchUserDetails = CypherReaderFunction.fetchUserDetails(token)
    val fetchUserDetailsResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(fetchUserDetails)).mapTo[Any]

    val results = Await.result(fetchUserDetailsResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result = UserDetails()
             for(res <- readResults.results){
               var x: UserDetails = res match {
                  case (Some(email),Some(firstName),Some(lastName)) => UserDetails(Option(email.asInstanceOf[String]),firstName.asInstanceOf[String],lastName.asInstanceOf[String])
                  case _ => null
                }
		if (x != null) {
		   result = x
                }

             }

            Logger.info("results "+result)
            result
      }
    }
   results
  }

  import models.Location
  def fetchLocationForActiveStream(inviteId: String): List[Location] = {

    val fetchLocationForActiveStream = CypherReaderFunction.fetchLocationForActiveStream(inviteId)
    val fetchLocationForActiveStreamResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(fetchLocationForActiveStream)).mapTo[Any]

    val results = Await.result(fetchLocationForActiveStreamResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result = List[Location]()
             for(res <- readResults.results){
	       System.out.println("----fetch-location--resuls"+res)
               var x: Location = res match {
                  case (Some(latitude),Some(longitude)) => 
		       				Location(latitude.asInstanceOf[Double],longitude.asInstanceOf[Double])
                  case _ => null
                }
		if (result != null) {
		   result = result :+ x
		}

             }

            Logger.info("results "+result)
            result
      }
    }
   results
  }


  import models.Location
  def fetchLocationForActiveStreamTwitter(inviteId: String): List[Location] = {

    val fetchLocationForActiveStreamTwitter = CypherReaderFunction.fetchLocationForActiveStreamTwitter(inviteId)
    val fetchLocationForActiveStreamResponseTwitter: Future[Any] = ask(neo4jreader, PerformReadOperation(fetchLocationForActiveStreamTwitter)).mapTo[Any]

    val results = Await.result(fetchLocationForActiveStreamResponseTwitter, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result = List[Location]()
             for(res <- readResults.results){
               var x: Location = res match {
                  case (Some(latitude),Some(longitude)) => 
		       				Location(latitude.asInstanceOf[Double],longitude.asInstanceOf[Double])
                  case _ => null
                }
		if (result != null) {
		   result = result :+ x
		}

             }

            Logger.info("results "+result)
            result
      }
    }
   results
  }


  import models.Location
  def fetchLocationForActiveStreamFacebook(inviteId: String): List[Location] = {

    val fetchLocationForActiveStreamFacebook = CypherReaderFunction.fetchLocationForActiveStreamFacebook(inviteId)
    val fetchLocationForActiveStreamResponseFacebook: Future[Any] = ask(neo4jreader, PerformReadOperation(fetchLocationForActiveStreamFacebook)).mapTo[Any]

    val results = Await.result(fetchLocationForActiveStreamResponseFacebook, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result = List[Location]()
             for(res <- readResults.results){
               var x: Location = res match {
                  case (Some(latitude),Some(longitude)) => 
		       				Location(latitude.asInstanceOf[Double],longitude.asInstanceOf[Double])
                  case _ => null
                }
		if (result != null) {
		   result = result :+ x
		}

             }

            Logger.info("results "+result)
            result
      }
    }
   results
  }

  def fetchLocationForActiveStreamLinkedin(inviteId: String): List[Location] = {

    val fetchLocationForActiveStreamLinkedin = CypherReaderFunction.fetchLocationForActiveStreamLinkedin(inviteId)
    val fetchLocationForActiveStreamResponseLinkedin: Future[Any] = ask(neo4jreader, PerformReadOperation(fetchLocationForActiveStreamLinkedin)).mapTo[Any]

    val results = Await.result(fetchLocationForActiveStreamResponseLinkedin, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result = List[Location]()
             for(res <- readResults.results){
               var x: Location = res match {
                  case (Some(latitude),Some(longitude)) => 
		       				Location(latitude.asInstanceOf[Double],longitude.asInstanceOf[Double])
                  case _ => null
                }
		if (result != null) {
		   result = result :+ x
		}

             }

            Logger.info("results "+result)
            result
      }
    }
   results
  }


  def  countAllTwitterInvites(token: String,clause: String): List[String] = {

    val countAllTwitterInvites = CypherReaderFunction.countAllTwitterInvites(token,clause)
    val countAllTwitterInvitesResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(countAllTwitterInvites)).mapTo[Any]

    var results = Await.result(countAllTwitterInvitesResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def  countAllFacebookInvites(token: String, clause: String): List[String] = {

    val countAllFacebookInvites = CypherReaderFunction.countAllFacebookInvites(token,clause)
    val countAllFacebookInvitesResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(countAllFacebookInvites)).mapTo[Any]

    var results = Await.result(countAllFacebookInvitesResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

  def  countAllLinkedinInvites(token: String,clause: String): List[String] = {

    val countAllLinkedinInvites = CypherReaderFunction.countAllLinkedinInvites(token,clause)
    val countAllLinkedinInvitesResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(countAllLinkedinInvites)).mapTo[Any]

    var results = Await.result(countAllLinkedinInvitesResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def getFacebookAcceptanceCount(token: String, clause: String): List[String] = {

    val getFacebookAcceptanceCount = CypherReaderFunction.getFacebookAcceptanceCount(token,clause)
    val getFacebookAcceptanceCountResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getFacebookAcceptanceCount)).mapTo[Any]

    var results = Await.result(getFacebookAcceptanceCountResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

  def getTwitterAcceptanceCount(token: String, clause: String): List[String] = {

    val getTwitterAcceptanceCount = CypherReaderFunction.getTwitterAcceptanceCount(token,clause)
    val getTwitterAcceptanceCountResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getTwitterAcceptanceCount)).mapTo[Any]

    var results = Await.result(getTwitterAcceptanceCountResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

 def getLinkedinAcceptanceCount(token: String,clause: String): List[String] = {

    val getLinkedinAcceptanceCount = CypherReaderFunction.getLinkedinAcceptanceCount(token,clause)
    val getLinkedinAcceptanceCountResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getLinkedinAcceptanceCount)).mapTo[Any]

    var results = Await.result(getLinkedinAcceptanceCountResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }


  def getReferersForLinkedin(stream: String): List[String] = {

    val getReferersForLinkedin = CypherReaderFunction.getReferersForLinkedin(stream)
    val getReferersForLinkedinResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getReferersForLinkedin)).mapTo[Any]

    val results = Await.result(getReferersForLinkedinResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result = List[String]()
             for(res <- readResults.results){
               var x: String = res match {
                  case Some(ip) => ip.asInstanceOf[String]
                  case _ => null
                }
		if (x != null) {
		  
		   result = result :+ x
                }

             }
            Logger.info("results "+result)
            result
      }
    }
   results
  }

  def getReferersForTwitter(stream: String): List[String] = {

    val getReferersForTwitter = CypherReaderFunction.getReferersForTwitter(stream)
    val getReferersForTwitterResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getReferersForTwitter)).mapTo[Any]

    val results = Await.result(getReferersForTwitterResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result = List[String]()
             for(res <- readResults.results){
               var x: String = res match {
                  case Some(ip) => ip.asInstanceOf[String]
                  case _ => null
                }
		if (x != null) {
		  
		   result = result :+ x
                }

             }
            Logger.info("results "+result)
            result
      }
    }
   results
  }

 def getReferersForFacebook(stream: String): List[String] = {

    val getReferersForFacebook = CypherReaderFunction.getReferersForFacebook(stream)
    val getReferersForFacebookResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getReferersForFacebook)).mapTo[Any]

    val results = Await.result(getReferersForFacebookResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {

             var result = List[String]()
             for(res <- readResults.results){
               var x: String = res match {
                  case Some(ip) => ip.asInstanceOf[String]
                  case _ => null
                }
		if (x != null) {
		  
		   result = result :+ x
                }

             }
            Logger.info("results "+result)
            result
      }
    }
   results
  }




}