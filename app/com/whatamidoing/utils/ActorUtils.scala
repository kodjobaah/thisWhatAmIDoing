package com.whatamidoing.utils

import akka.actor.ActorSystem
import com.whatamidoing.cypher.CypherReaderFunction
import akka.pattern.ask
import akka.pattern.AskTimeoutException
import akka.util.Timeout
import scala.concurrent.Await
import controllers.WhatAmIDoingController
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.future
import com.whatamidoing.cypher.CypherWriterFunction
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.whatamidoing.actors.neo4j.Neo4JWriter._
import play.api.Logger
import com.whatamidoing.actors.red5.FrameSupervisor
import com.whatamidoing.actors.neo4j.Neo4JWriter
import com.whatamidoing.actors.neo4j.Neo4JReader

object ActorUtils {

  val system = ActorSystem("whatamidoing-system")
  implicit val timeout = Timeout(500 seconds)
  var frameSupervisor = system.actorOf(FrameSupervisor.props("hey"), "frameSupervisor")
  var neo4jwriter = system.actorOf(Neo4JWriter.props(), "neo-4j-writer-supervisor")
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

  import play.api.mvc.Results._
  def createUser(fn: String, ln: String, em: String, p: String) = {

    val createUser = CypherWriterFunction.createUser(fn, ln, em, p);

    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createUser)).mapTo[Any]

    var writeResult: scala.concurrent.Future[play.api.mvc.SimpleResult] = writeResponse.flatMap(
      {
        case WriteOperationResult(results) => {

          var res = ActorUtils.getUserToken(em)
          if (res != "-1") {
            future(Ok("USER CREATED - ADDED AUTHENTICATION TOKEN TO SESSISON").withSession(
              "whatAmIdoing-authenticationToken" -> res))
          } else {
            future(Ok("USER CREATE - AUTHENTICATION TOKEN NOT ADDED"))
          }
        }

      })
    writeResult
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


  def createInvite(stream: String, email: String, id: String) = {
    val createInvite = CypherWriterFunction.createInvite(stream, email, id)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createInvite)).mapTo[Any]

    var streamName = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
          results.results.head.asInstanceOf[String]
        } else {
          ""

        }
      }
    }
    streamName
  }

  def createInviteTwitter(stream: String, twitter: String, id: String) = {
    val createInviteTwitter= CypherWriterFunction.createInviteTwitter(stream, twitter, id)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createInviteTwitter)).mapTo[Any]

    var inviteId = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
         Logger("ActorUtils.createInviteTwitter").info("results size["+results.results.size+"]");
         Logger("ActorUtils.createInviteTwitter").info("resuls["+results.results.head+"]");

        } else {
          ""

        }
      }
    }
    inviteId
  }

  def createInviteFacebook(stream: String, facebook: String, id: String) = {
    val createInviteFacebook= CypherWriterFunction.createInviteFacebook(stream, facebook, id)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createInviteFacebook)).mapTo[Any]

    var inviteId = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
         Logger("ActorUtils.createInviteTwitter").info("resuls["+results.results.head+"]");

        } else {
          ""

        }
      }
    }
    inviteId
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



  def invalidateToken(token: String) = {

    val invalidateToken = CypherWriterFunction.invalidateToken(token)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(invalidateToken)).mapTo[Any]

    var res = Await.result(writerResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
          results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    res
  }

  def invalidateAllTokensForUser(email: String) = {

    val invalidateToken = CypherWriterFunction.invalidateAllTokensForUser(email)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(invalidateToken)).mapTo[Any]

    var res = Await.result(writerResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
          results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    res
  }

  def createTokenForUser(token: String, email: String) = {

    Logger("ActorUtils.createTokenForUser").info("token["+token+"] email["+email+"]")
    val createTokenForUser = CypherWriterFunction.createTokenForUser(token, email)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createTokenForUser)).mapTo[Any]

    var res = Await.result(writerResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
        	results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    res
  }

  def associatedInviteWithDayOfAcceptance(inviteId: String) = {
    val associateInviteWithAcceptanceDay = CypherWriterFunction.associateDayWithInvite(inviteId)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(associateInviteWithAcceptanceDay)).mapTo[Any]

    var res = Await.result(writerResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
        results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    res

  }

  def associatedInviteTwitterWithReferer(inviteId: String, referal: String) = {
    val associateInviteTwitterWithReferer = CypherWriterFunction.associateInviteTwitterWithReferer(inviteId,referal)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(associateInviteTwitterWithReferer)).mapTo[Any]

    var res = Await.result(writerResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
        results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    res

  }

  def associatedInviteFacebookWithReferer(inviteId: String, referal: String) = {
    val associateInviteFacebookWithReferer = CypherWriterFunction.associateInviteFacebookWithReferer(inviteId,referal)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(associateInviteFacebookWithReferer)).mapTo[Any]

    var res = Await.result(writerResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
        results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    res

  }


  
  def createStream(token: String, streamName: String): String = {
    // Logger("FrameSupervisor-receive").info("creating actor for token:"+streamName)

    var stream = CypherWriterFunction.createStream(streamName, token)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(stream)).mapTo[Any]

    var res = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
    res
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
  
 def closeStream(streamName: String) = {
    var closeStream = CypherWriterFunction.closeStream(streamName)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(closeStream)).mapTo[Any]

    var res = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
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


  def changePasswordRequest(email: String, changePasswordId: String): String = {

    var stream = CypherWriterFunction.changePasswordRequest(email, changePasswordId)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(stream)).mapTo[Any]

    var res = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
    res
  }

  def updatePassword(cpId: String, newPassword: String): String = {

    var stream = CypherWriterFunction.updatePassword(cpId, newPassword)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(stream)).mapTo[Any]

    var res = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
    res
  }

 def deactivatePreviousChangePasswordRequest(email: String): String = {

    var stream = CypherWriterFunction.deactivatePreviousChangePasswordRequest(email)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(stream)).mapTo[Any]

    var res = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
    res
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

	       System.out.println("----fetch--resuls"+res)
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

 def updateUserDetails(token:String, firstName: String, lastName: String): String = {

    var updateUserDetails = CypherWriterFunction.updateUserDetails(token,firstName,lastName)
    val updateUserDetailsResponse: Future[Any] = ask(neo4jwriter, PerformOperation(updateUserDetails)).mapTo[Any]

    var res = Await.result(updateUserDetailsResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
    res
  }

  def createLocationForStream(token: String, latitude: Double, longitude: Double): String = {
    var createLocationForStream = CypherWriterFunction.createLocationForStream(token,latitude,longitude)
    val createLocationForStreamResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createLocationForStream)).mapTo[Any]

    var res = Await.result(createLocationForStreamResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
    res
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
  def fetchLocationForActiveStreamFacebook(inviteId: String): List[Location] = {

    val fetchLocationForActiveStreamFacebook = CypherReaderFunction.fetchLocationForActiveStreamFacebook(inviteId)
    val fetchLocationForActiveStreamResponseFacebook: Future[Any] = ask(neo4jreader, PerformReadOperation(fetchLocationForActiveStreamFacebook)).mapTo[Any]

    val results = Await.result(fetchLocationForActiveStreamResponseFacebook, 30 seconds) match {
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


  def invalidateAllStreams(token: String) = {

    val invalidateAllStreams = CypherWriterFunction.invalidateAllStreams(token)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(invalidateAllStreams)).mapTo[Any]

    var res = Await.result(writerResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        if (results.results.size > 0) {
          results.results.head.asInstanceOf[String]
        } else {
          ""
        }
      }
    }
    res
  }



  def  countAllTwitterInvites(token: String): List[String] = {

    val countAllTwitterInvites = CypherReaderFunction.countAllTwitterInvites(token)
    val countAllTwitterInvitesResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(countAllTwitterInvites)).mapTo[Any]

    var results = Await.result(countAllTwitterInvitesResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

  def  countAllFacebookInvites(token: String): List[String] = {

    val countAllFacebookInvites = CypherReaderFunction.countAllFacebookInvites(token)
    val countAllFacebookInvitesResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(countAllFacebookInvites)).mapTo[Any]

    var results = Await.result(countAllFacebookInvitesResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

  def getFacebookAcceptanceCount(token: String): List[String] = {

    val getFacebookAcceptanceCount = CypherReaderFunction.getFacebookAcceptanceCount(token)
    val getFacebookAcceptanceCountResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getFacebookAcceptanceCount)).mapTo[Any]

    var results = Await.result(getFacebookAcceptanceCountResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

  def getTwitterAcceptanceCount(token: String): List[String] = {

    val getTwitterAcceptanceCount = CypherReaderFunction.getTwitterAcceptanceCount(token)
    val getTwitterAcceptanceCountResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getTwitterAcceptanceCount)).mapTo[Any]

    var results = Await.result(getTwitterAcceptanceCountResponse, 30 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.asInstanceOf[List[String]]
      }
    }
    results
  }

}