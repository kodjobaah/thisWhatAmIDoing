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

class FrameSupervisor(username: String) extends Actor {

  
  val videoEncoders = scala.collection.mutable.Map[String,ActorRef]()
  
  val system = ActorUtils.system
  import FrameSupervisor._
  import com.whatamidoing.actors.red5.VideoEncoder._
  override def receive: Receive = {
    case RTMPMessage(message,token) => {
      
      var videoEncoder: ActorRef = videoEncoders get token match {
    					case None => {
    						val ve = system.actorOf(VideoEncoder.props(token+".flv"), "rtmpsender")
    						videoEncoders += token -> ve
    						ve
    					}
    					case videoEncoder => videoEncoder.get 
      				
      				}
        videoEncoder ! EncodeFrame(message)
    }
     
    case StopVideo(token) => {
      
       videoEncoders get token match {
    					case None => {
    					}
    					case videoEncoder => {
    					  system.stop(videoEncoder.get) 
    					  videoEncoders -= token
    					}
      				
      	}
    
    }
  }
}

object FrameSupervisor {

  def props(username: String) = Props(new FrameSupervisor(username))

  case class RTMPMessage(val message: String, val token: String)
  
  case class StopVideo(val token: String)

}
