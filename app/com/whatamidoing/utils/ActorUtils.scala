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

object ActorUtils {

  val system = ActorSystem("whatamidoing-system")
  implicit val timeout = Timeout(500 seconds)
  import com.whatamidoing.actors.neo4j.Neo4JReader._
  def getUserToken(em: String) = {
    val getUserToken = CypherReaderFunction.getUserToken(em)
    //Used by ?(ask)

    val getUserTokenResponse: Future[Any] = ask(WhatAmIDoingController.neo4jreader, PerformReadOperation(getUserToken)).mapTo[Any]
    var res = Await.result(getUserTokenResponse, 10 seconds) match {
      case ReadOperationResult(results) => {
        if (results.results.size > 0) {
          val tok = results.results.head.asInstanceOf[(String, String)]
          if (tok._2 == "true") {
            tok._1
          } else {
            "-1"
          }
        }
        "-1"
      }
    }
    res
  }

  import play.api.mvc.Results._
  def createUser(fn: String, ln: String, em: String, p: String) = {

    val createUser = CypherWriterFunction.createUser(fn, ln, em, p);

    val writeResponse: Future[Any] = ask(WhatAmIDoingController.neo4jwriter, PerformOperation(createUser)).mapTo[Any]

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
    val response: Future[Any] = ask(WhatAmIDoingController.neo4jreader, PerformReadOperation(searchForUser)).mapTo[Any]

    var res = Await.result(response, 10 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results.mkString
      }
    }
    res
  }

  def streamNameForToken(token: String) = {
    val findStreamForToken = CypherReaderFunction.findActiveStreamForToken(token)
    val findStreamForTokenResponse: Future[Any] = ask(WhatAmIDoingController.neo4jreader, PerformReadOperation(findStreamForToken)).mapTo[Any]
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
    val getValidTokenResponse: Future[Any] = ask(WhatAmIDoingController.neo4jreader, PerformReadOperation(getValidToken)).mapTo[Any]

    var res = Await.result(getValidTokenResponse, 10 seconds) match {
      case ReadOperationResult(readResults) => {
        readResults.results
      }
    }

    res
  }

  def createInvite(stream: String, email: String, id: String) = {
    val createInvite = CypherWriterFunction.createInvite(stream, email, id)
    val writeResponse: Future[Any] = ask(WhatAmIDoingController.neo4jwriter, PerformOperation(createInvite)).mapTo[Any]

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
    val readerResponse: Future[Any] = ask(WhatAmIDoingController.neo4jreader, PerformReadOperation(createInvite)).mapTo[Any]

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
    val writerResponse: Future[Any] = ask(WhatAmIDoingController.neo4jwriter, PerformOperation(invalidateToken)).mapTo[Any]

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

    val createTokenForUser = CypherWriterFunction.createTokenForUser(token, email)
    val writerResponse: Future[Any] = ask(WhatAmIDoingController.neo4jwriter, PerformOperation(createTokenForUser)).mapTo[Any]

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
    val writerResponse: Future[Any] = ask(WhatAmIDoingController.neo4jwriter, PerformOperation(associateInviteWithAcceptanceDay)).mapTo[Any]

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
    val writeResponse: Future[Any] = ask(WhatAmIDoingController.neo4jwriter, PerformOperation(stream)).mapTo[Any]

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
    val getValidTokenResponse: Future[Any] = ask(WhatAmIDoingController.neo4jreader, PerformReadOperation(findStreamForToken)).mapTo[Any]

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
    val writeResponse: Future[Any] = ask(WhatAmIDoingController.neo4jwriter, PerformOperation(closeStream)).mapTo[Any]

    var res = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
  }

}