package com.whatamidoing.actors.red5

import java.io.ByteArrayInputStream
import com.whatamidoing.actors.red5.services.Xuggler

import akka.actor.Actor
import akka.actor.ActorRef
import play.api.Logger

import akka.actor.Props
import javax.imageio.ImageIO
import sun.misc.BASE64Decoder
import com.whatamidoing.utils.ActorUtils
import com.whatamidoing.utils.ActorUtilsReader
import play.api.libs.json._
import com.whatamidoing.cypher.CypherWriterFunction
import scala.concurrent.Await
import scala.concurrent._
import scala.concurrent.duration.DurationInt
import akka.pattern.ask
import akka.pattern.AskTimeoutException
import akka.util.Timeout
import controllers.WhatAmIDoingController
import com.whatamidoing.cypher.CypherReaderFunction

import models.Messages._

class FrameSupervisor(username: String) extends Actor {

  //Used by ?(ask)
  implicit val timeout = Timeout(1 seconds)
  val videoEncoders = scala.collection.mutable.Map[String, ActorRef]()

  val system = ActorUtils.system
  
  override def receive: Receive = {
    case RTMPMessage(message, token) => {

      var videoEncoder: ActorRef = videoEncoders get token match {
        case None => {
          var  sn = token + "--" + java.util.UUID.randomUUID.toString
          var streamName = sn  + ".flv"
          //var streamName = token + "--" + java.util.UUID.randomUUID.toString
	  ActorUtils.invalidateAllStreams(token)
	  val userInformation = ActorUtilsReader.fetchUserInformation(token)
          Logger("FrameSupervisor").info("domId:" + userInformation.domId)

   	  import play.api.Play
      	  implicit var currentPlay = Play.current
  	  val xmppDomain = Play.current.configuration.getString("xmpp.domain").get
          var domId = ""
	  if (userInformation.domId == None) {
             domId = java.util.UUID.randomUUID.toString
	     ActorUtils.updateUserInformation(token,domId)
             import com.whatamidoing.services.xmpp.AddHocCommands
	     val domain = domId+"."+xmppDomain
	     ActorUtils.createXmppDomain(domain)

	  } else {
	    domId = userInformation.domId.get
	  }

          var res = ActorUtils.createStream(token, streamName)

      	 // val xmppHost = Play.current.configuration.getString("xmpp.host").get

	  val roomJid = sn + "@muc."+domId+"."+xmppDomain
	  Logger.info("--------- ROOOM ID:"+roomJid)
	  ActorUtils.associateRoomWithStream(token,roomJid)
	  ActorUtils.createXmppGroup(roomJid,token)
          
          Logger("FrameSupervisor.receive").info("results from creating stream:" + res)
          val ve = system.actorOf(VideoEncoder.props(streamName), "videoencoder:" + java.util.UUID.randomUUID.toString)
          videoEncoders += token -> ve
          ve
        }
        case videoEncoder => videoEncoder.get

      }
      //Logger("FrameSupervisor.receive:").info("send message to be encoded");
      videoEncoder ! EncodeFrame(message)
    }

    case StopVideo(token) => {

      videoEncoders get token match {
        
      	case None => {
          Logger("FrameSupervisor.receive").info("ACTOR NOT FOUND-- NOT STOPPING:" + token + "]");
        }
        
        case videoEncoder => {
          
          Logger("FrameSupervisor.receive").info("ACTOR FOUND STOPPING [:" + token + "]");
          system.stop(videoEncoder.get)
          videoEncoders -= token

          var streamName = ActorUtilsReader.findActiveStreamForToken(token)
          Logger("FrameSupervisor.receive").info("stream name:" + streamName)
          if (!streamName.isEmpty()) {
	    var roomJid =  ActorUtilsReader.getRoomJid(token)
          Logger("FrameSupervisor.receive").info("ROOM JID [:" + roomJid + "]");
	    ActorUtils.removeRoom(roomJid)
            var res = ActorUtils.closeStream(streamName)
          }

        }

      }

    }
    
    case x => Logger("FrameSupervisor.receive").info("DEFAULT_MESSAGE:"+x.toString())
    }
}

object FrameSupervisor {

  def props(username: String) = Props(new FrameSupervisor(username))


}

