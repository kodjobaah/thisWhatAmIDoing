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







}