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

class FrameSupervisor(username: String) extends Actor {

    //Used by ?(ask)
  implicit val timeout = Timeout(1 seconds)
  val videoEncoders = scala.collection.mutable.Map[String,ActorRef]()
  
  val system = ActorUtils.system
  import FrameSupervisor._
  import com.whatamidoing.actors.red5.VideoEncoder._
  override def receive: Receive = {
    case RTMPMessage(message,token) => {
      
      var videoEncoder: ActorRef = videoEncoders get token match {
    					case None => {
    					     import com.whatamidoing.actors.neo4j.Neo4JWriter._
    					    var streamName = token+":"+java.util.UUID.randomUUID.toString+".flv"
    					   // Logger("FrameSupervisor-receive").info("creating actor for token:"+streamName)
    					    
    					    var stream = CypherWriterFunction.createStream(streamName, token)
    						val writeResponse: Future[Any] = ask(WhatAmIDoingController.neo4jwriter, PerformOperation(stream)).mapTo[Any]

    					    var res = Await.result(writeResponse, 10 seconds) match {
    					                case WriteOperationResult(results) => {
    					                		results.results.mkString
    						 		}
    					    }
    					    Logger("FrameSupervisor.receive").info("results from creating stream:"+res)
    					    val ve = system.actorOf(VideoEncoder.props(streamName), "videoencoder:"+java.util.UUID.randomUUID.toString)
    						videoEncoders += token -> ve
    						ve
    					}
    					case videoEncoder => videoEncoder.get 
      				
      				}
      	//Logger("FrameSupervisor.receive:").info("send message to be encoded");
        videoEncoder ! EncodeFrame( (message \ "frame").as[String])
    }
     
    case StopVideo(token) => {
      
       videoEncoders get token match {
    					case None => {
    					  Logger("FrameSupervisor.receive").info("ACTOR NOT FOUND-- NOT STOPPING:"+token+"]");
    					}
    					case videoEncoder => {
    					  val findStreamForToken = CypherReaderFunction.findActiveStreamForToken(token)
    				      import com.whatamidoing.actors.neo4j.Neo4JReader._
    				      val getValidTokenResponse: Future[Any] = ask(WhatAmIDoingController.neo4jreader, PerformReadOperation(findStreamForToken)).mapTo[Any]

    					   var streamName = Await.result(getValidTokenResponse, 10 seconds) match {
    				           case ReadOperationResult(readResults) => {
    				        	   readResults.results.head.asInstanceOf[String]
    				           }
    					  }
    					  
    					  Logger("FrameSupervisor.receive").info("stream name:"+streamName)
    					  
    					   
    					    import com.whatamidoing.actors.neo4j.Neo4JWriter._
    					    Logger("FrameSupervisor.receive").info("ACTOR FOUND STOPPING [:"+token+"]");
      				        system.stop(videoEncoder.get) 
    					    videoEncoders -= token
    					   
    					    var closeStream = CypherWriterFunction.closeStream(streamName)
    						val writeResponse: Future[Any] = ask(WhatAmIDoingController.neo4jwriter, PerformOperation(closeStream)).mapTo[Any]

    					    var res = Await.result(writeResponse, 10 seconds) match {
    					                case WriteOperationResult(results) => {
    					                		results.results.mkString
    						 		}
    					    }

    					  
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

