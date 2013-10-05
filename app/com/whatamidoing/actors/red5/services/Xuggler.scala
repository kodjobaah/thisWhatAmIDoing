package com.whatamidoing.actors.red5.services

import com.xuggle.xuggler.IStreamCoder
import com.xuggle.xuggler.IContainer
import com.xuggle.xuggler.IContainerFormat
import com.xuggle.xuggler.IStream
import com.xuggle.xuggler.IVideoResampler
import com.xuggle.xuggler.IAudioResampler
import com.xuggle.xuggler.ICodec
import com.xuggle.xuggler.IPixelFormat
import com.xuggle.xuggler.IRational
import com.xuggle.xuggler.IPacket
import com.xuggle.xuggler.IVideoPicture
import com.xuggle.xuggler.IAudioSamples
import com.xuggle.mediatool.IMediaWriter
import com.xuggle.mediatool.ToolFactory

import java.awt.image.BufferedImage
import play.Logger

class Xuggler(outputUrl: String) {

  //Accessing the constants
  import Xuggler._

  val mediaWriter: IMediaWriter =
               ToolFactory.makeWriter("rtmp://localhost:1935/oflaDemo/kodjo.flv")
      mediaWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_FLV1,352, 288)
      //mediaWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_FLV1,640, 480)
	 
	  val startTime = System.nanoTime()
	 
          var count = 0
          def transmitBufferedImage(image: BufferedImage) {
	    import java.util.concurrent.TimeUnit
            import javax.imageio.ImageIO
	    import java.io.File
	    import play.api.Logger
		//Logger.info("ABOUT TO CREATE FILE");
              if (count == 1) {

		Logger.info("CREATING FILE");
 //          val outputfile = new File("/tmp/image.jpg")
//ImageIO.write(image, "jpg", outputfile)
               }
		count = count + 1
	    mediaWriter.encodeVideo(0, image, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

          }
	  def transmitFrame(frame: Array[Byte]) = {

	    // convert byte array back to BufferedImage
	    import java.io.InputStream
	    import java.io.ByteArrayInputStream
	    import java.awt.image.BufferedImage
	    import javax.imageio.ImageIO
	    import play.api.Logger
	    import java.util.concurrent.TimeUnit
		val in: InputStream  = new ByteArrayInputStream(frame);
		
  //          Logger("HMM").info("inputstream:"+in);
	    val bImageFromConvert: BufferedImage  = ImageIO.read(in);
   //         Logger("MyApp").info("just before sending %s".format(bImageFromConvert))

	   if (bImageFromConvert != null)
	    mediaWriter.encodeVideo(0, bImageFromConvert, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
	  
	  }

}

object Xuggler {
  
  def apply(username: String) = new Xuggler(username)

}
