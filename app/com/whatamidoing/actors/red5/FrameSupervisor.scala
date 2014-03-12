package com.whatamidoing.actors.red5

import java.io.ByteArrayInputStream
import com.whatamidoing.actors.red5.services.Xuggler
import akka.actor.Actor
import akka.actor.Props
import javax.imageio.ImageIO
import play.api.Logger
import sun.misc.BASE64Decoder
import com.whatamidoing.utils.ActorUtils
import akka.actor.ActorRef
import play.api.Logger
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
          var streamName = token + "--" + java.util.UUID.randomUUID.toString + ".flv"
          //var streamName = token + "--" + java.util.UUID.randomUUID.toString
          var res = ActorUtils.createStream(token, streamName)
          
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

          var streamName = ActorUtils.findActiveStreamForToken(token)
          Logger("FrameSupervisor.receive").info("stream name:" + streamName)
          if (!streamName.isEmpty()) {
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

