package com.whatamidoing.actors.red5

import java.io.ByteArrayInputStream

import com.whatamidoing.actors.red5.services.Xuggler

import akka.actor.Actor
import akka.actor.Props
import javax.imageio.ImageIO
import play.api.Logger
import sun.misc.BASE64Decoder

class RTMPSender(username: String) extends Actor {

  import RTMPSender._
  val xuggler = Xuggler(username)

  override def receive: Receive = {
    case RTMPMessage(message) => {
      import sun.misc.BASE64Decoder
      val base64: BASE64Decoder = new BASE64Decoder();
      val bytes64: Array[Byte] = base64.decodeBuffer(message);
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
  }
}

object RTMPSender {

  def props(username: String) = Props(new RTMPSender(username))

  case class RTMPMessage(val message: String)

}

