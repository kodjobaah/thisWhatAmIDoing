package com.whatamidoing.actors.red5

import akka.actor.Actor
import akka.actor.Props

import com.whatamidoing.actors.red5.services.Xuggler

class VideoEncoder(streamName: String) extends Actor {

  def nameOfStream = streamName
  val xuggler = Xuggler(streamName)
  
  import VideoEncoder._
   override def receive: Receive = {
 
      case EncodeFrame(frame) => {
      import sun.misc.BASE64Decoder
      val base64: BASE64Decoder = new BASE64Decoder();
      val bytes64: Array[Byte] = base64.decodeBuffer(frame);
      import java.io.ByteArrayInputStream
      val bais: ByteArrayInputStream = new ByteArrayInputStream(bytes64)

      import javax.imageio.ImageIO
      try {
    	  var bufferedImage = ImageIO.read(bais);
    	  //Logger("MyApp").info("--converted buffered image:" + bufferedImage)
    	  xuggler.transmitBufferedImage(bufferedImage);
      } catch {
        case ex: Throwable => {
          println(ex)
        }
      }

      }
      case EndTransmission => {
        
      }
   }
   
}

object VideoEncoder {
  
  def props(streamName: String) = Props(new VideoEncoder(streamName))
  
  case class EncodeFrame(frame: String)
  case class EndTransmission
  
}