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
class FrameSupervisor(username: String) extends Actor {

  
  val videoEncoders = scala.collection.mutable.Map[String,ActorRef]()
  
  val system = ActorUtils.system
  import FrameSupervisor._
  import com.whatamidoing.actors.red5.VideoEncoder._
  override def receive: Receive = {
    case RTMPMessage(message,token) => {
      
      var videoEncoder: ActorRef = videoEncoders get token match {
    					case None => {
    					    Logger("FrameSupervisor-receive").info("creating actor for token:"+token)
    						val ve = system.actorOf(VideoEncoder.props(token+".flv"), "videoencoder:"+java.util.UUID.randomUUID.toString)
    						videoEncoders += token -> ve
    						ve
    					}
    					case videoEncoder => videoEncoder.get 
      				
      				}
      	Logger("FrameSupervisor.receive:").info("send message to be encoded");
        videoEncoder ! EncodeFrame( (message \ "frame").as[String])
    }
     
    case StopVideo(token) => {
      
       videoEncoders get token match {
    					case None => {
    					  Logger("FrameSupervisor.receive").info("ACTOR NOT FOUND-- NOT STOPPING:"+token+"]");
    					}
    					case videoEncoder => {
    					   Logger("FrameSupervisor.receive").info("ACTOR FOUND STOPPING [:"+token+"]");
      				       system.stop(videoEncoder.get) 
    					   videoEncoders -= token
    					}
      				
      	}
    
    }
  }
}

object FrameSupervisor {

  def props(username: String) = Props(new FrameSupervisor(username))

  case class RTMPMessage(val message: JsValue, val token: String)
  
  case class StopVideo(val token: String)

}

