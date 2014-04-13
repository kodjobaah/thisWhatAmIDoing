package com.whatamidoing.utils

import akka.actor.ActorSystem
import com.whatamidoing.cypher.CypherReaderFunction
import akka.pattern.ask
import akka.pattern.AskTimeoutException
import akka.util.Timeout
import scala.concurrent.Await

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.future
import com.whatamidoing.cypher.CypherWriterFunction
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import play.api.Logger
import com.whatamidoing.actors.red5.FrameSupervisor
import com.whatamidoing.actors.xmpp.XmppSupervisor
import com.whatamidoing.actors.neo4j.Neo4JWriter
import com.whatamidoing.actors.neo4j.Neo4JWriter._
import com.whatamidoing.actors.neo4j.Neo4JReader
import controllers.WhatAmIDoingController



object ActorUtils {

  val system = ActorSystem("whatamidoing-system")
  implicit val timeout = Timeout(10 seconds)
  var frameSupervisor = system.actorOf(FrameSupervisor.props("hey"), "frameSupervisor")
  var xmppSupervisor = system.actorOf(XmppSupervisor.props(), "xmppSupervisor")
  var neo4jwriter = system.actorOf(Neo4JWriter.props(), "neo-4j-writer-supervisor")
  var neo4jreader = system.actorOf(Neo4JReader.props(), "neo-4j-reader-supervisor")
  
  import models.Messages._
  

  import play.api.mvc.Results._
  def createUser(fn: String, ln: String, em: String, p: String) = {

    val createUser = CypherWriterFunction.createUser(fn, ln, em, p);

    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createUser)).mapTo[Any]

    var writeResult: scala.concurrent.Future[play.api.mvc.SimpleResult] = writeResponse.flatMap(
      {
        case WriteOperationResult(results) => {

          var res = ActorUtilsReader.getUserToken(em)
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
  def createInviteLinkedin(stream: String, linkedin: String, id: String) = {
    val createInviteLinkedin= CypherWriterFunction.createInviteLinkedin(stream, linkedin, id)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createInviteLinkedin)).mapTo[Any]

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

  def associatedInviteTwitterWithReferer(inviteId: String, referal: String, sessionId: String) = {
    val associateInviteTwitterWithReferer = CypherWriterFunction.associateInviteTwitterWithReferer(inviteId,referal,sessionId)
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

  def associatedInviteLinkedinWithReferer(inviteId: String, referal: String) = {
    val associateInviteLinkedinWithReferer = CypherWriterFunction.associateInviteLinkedinWithReferer(inviteId,referal)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(associateInviteLinkedinWithReferer)).mapTo[Any]

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
  
  
 def closeStream(streamName: String) = {
    var closeStream = CypherWriterFunction.closeStream(streamName)
    val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(closeStream)).mapTo[Any]

    var res = Await.result(writeResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
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


  def associateRoomWithStream(token: String, roomId: String): String = {
    var associateRoomWithStream = CypherWriterFunction.associateRoomWithStream(token,roomId)
    val associateRoomWithStreamResponse: Future[Any] = ask(neo4jwriter, PerformOperation(associateRoomWithStream)).mapTo[Any]

    var res = Await.result(associateRoomWithStreamResponse, 10 seconds) match {
      case WriteOperationResult(results) => {
        results.results.mkString
      }
    }
    res
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


  def updateUserInformation(token: String, domId: String) = {

    val updateUserInformation = CypherWriterFunction.updateUserInformation(token,domId)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(updateUserInformation)).mapTo[Any]

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

  def createXmppDomain(domain: String) {
      import models.Messages.CreateXMPPDomainMessage
      val mess = CreateXMPPDomainMessage(domain)
      
      val response: Future[Any] = ask(xmppSupervisor, mess).mapTo[Any]
      import models.Messages.Done
      var res = Await.result(response, 10 seconds) match {
      	  case Done(results) => {
      	  Logger.info("--------results from creating---:"+results)
      }
    }


  }

 
  def createXmppGroup(roomJid: String, token: String) = {
    import com.whatamidoing.actors.xmpp.CreateXMPPGroup
    import models.Messages.CreateXMPPGroupMessage
    val message = CreateXMPPGroupMessage(roomJid,token)
    xmppSupervisor ! message
  }


  def createXmppRoom(roomJid: String) = {
    import com.whatamidoing.actors.xmpp.CreateXMPPGroup
    import models.Messages.CreateXMPPRoomMessage
    val message = CreateXMPPRoomMessage(roomJid)
    xmppSupervisor ! message
  }

  def removeRoom(roomJid: String) = {
    import models.Messages.RemoveXMPPRoomMessage
    val message =  RemoveXMPPRoomMessage(roomJid)
    xmppSupervisor ! message
  }

  def videoStreamStartedSocialMedia(sessionId: String) = {
    val videoStreamStartedSocialMedia = CypherWriterFunction.videoStreamStartedSocialMedia(sessionId)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(videoStreamStartedSocialMedia)).mapTo[Any]

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

  def videoStreamStoppedSocialMedia(sessionId: String) = {
    val videoStreamStoppedSocialMedia = CypherWriterFunction.videoStreamStoppedSocialMedia(sessionId)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(videoStreamStoppedSocialMedia)).mapTo[Any]

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

 def deactivateAllRefererStreamActions(sessionId: String) = {
    val deactivateAllRefererStreamActions = CypherWriterFunction.deactivateAllRefererStreamActions(sessionId)
    val writerResponse: Future[Any] = ask(neo4jwriter, PerformOperation(deactivateAllRefererStreamActions)).mapTo[Any]

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

}