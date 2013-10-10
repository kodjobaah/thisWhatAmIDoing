package integration.controller

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite
import scala.concurrent.duration._
import play.api.mvc.Cookie
import play.api.mvc.HandlerRef
import play.core.Router.HandlerDef
import play.core.Router
import play.api.mvc.AsyncResult
import akka.util.Timeout
import akka.actor.Actor
import org.mindrot.jbcrypt.BCrypt
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.FlatSpec
import org.eclipse.jetty.websocket.WebSocketClientFactory
import play.api.Logger
import play.api.mvc.SimpleResult
import com.ning.http.client.Request
import java.net.URI
import oracle.net.aso.s
import java.util.concurrent.TimeUnit
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.websocket.WebSocketUpgradeHandler
import com.ning.http.client.websocket.WebSocketTextListener
import org.eclipse.jetty.websocket.WebSocket.Connection
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import com.ning.http.client.websocket.WebSocket
import integration.util.SetupNeo4jActorsStub
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import com.whatamidoing.mail.EmailSenderService
import org.scalatest.mock.MockitoSugar

//import org.java_websocket.WebSocket

//import com.ning.http.client.websocket.WebSocket


class WhatAmIdoingControllerSpec extends FlatSpec  with MockitoSugar with SetupNeo4jActorsStub with Matchers {

  "when a user is invited to join and token is not valid" should "not be sent an email" in {
    running(TestServer(3333)) {
        currentTest = "invitedToViewTokenNotValid"
        val fakeRequest = FakeRequest()
         val mockEmailService = mock[EmailSenderService]
         controllers.WhatAmIDoingController.emailSenderService = mockEmailService
          val testToken = "testToken"
         //val someResult = play.api.test.Helpers.await(controllers.WhatAmIDoingController.invite(email.get,testToken)(fakeRequest))
         val someResult = controllers.WhatAmIDoingController.invite(testToken,email.get)(fakeRequest)

        import org.mockito.Mockito._
        import org.mockito.Matchers._
        
        verify(mockEmailService,never()).sendRegistrationEmail(org.mockito.Matchers.eq(email.get), any())
        verify(mockEmailService,never()).sendInviteEmail(org.mockito.Matchers.eq(email.get), any())
              
        contentAsString(someResult) should include ("Unable To Invite")
      
    }
  }
  "when a user is invited to join and stream does not exist" should "not be sent an email" in {
    running(TestServer(3333)) {
        currentTest = "invitedToViewStreamButDoesNotExist"
        val fakeRequest = FakeRequest()
         val mockEmailService = mock[EmailSenderService]
         controllers.WhatAmIDoingController.emailSenderService = mockEmailService
          val testToken = "testToken"
         //val someResult = play.api.test.Helpers.await(controllers.WhatAmIDoingController.invite(email.get,testToken)(fakeRequest))
         val someResult = controllers.WhatAmIDoingController.invite(testToken,email.get)(fakeRequest)

        import org.mockito.Mockito._
        import org.mockito.Matchers._
        
        verify(mockEmailService,never()).sendRegistrationEmail(org.mockito.Matchers.eq(email.get), any())
        verify(mockEmailService,never()).sendInviteEmail(org.mockito.Matchers.eq(email.get), any())
              
        contentAsString(someResult) should include ("Unable to Invite No Stream")
      
    }
    
  }
  "when a user is invited to join and is not regitered" should "registered user and create invited" in {
    running(TestServer(3333)) {
        val fakeRequest = FakeRequest()
        currentTest = "invitedToJoinAndRegistered"
         val mockEmailService = mock[EmailSenderService]
         controllers.WhatAmIDoingController.emailSenderService = mockEmailService
          val testToken = "testToken"
         //val someResult = play.api.test.Helpers.await(controllers.WhatAmIDoingController.invite(email.get,testToken)(fakeRequest))
         val someResult = controllers.WhatAmIDoingController.invite(testToken,email.get)(fakeRequest)

        import org.mockito.Mockito._
        import org.mockito.Matchers._
        
        verify(mockEmailService).sendRegistrationEmail(org.mockito.Matchers.eq(email.get), any())
        verify(mockEmailService).sendInviteEmail(org.mockito.Matchers.eq(email.get), any())
              
        contentAsString(someResult) should include ("Done")
        println(someResult)
    }
  } 
  
  "when a user is registered" should "not return authentication token if passoword is valid but token is invalid" in {
      running(TestServer(3333)) {
        val fakeRequest = FakeRequest()
        currentTest = "registeredLoginWithValidPasswordInvalidToken"
        val someResult = controllers.WhatAmIDoingController.registerLogin(email, password, firstName, lastName)(fakeRequest)

        val cookies = Helpers.cookies(someResult)
        val play_session = cookies.get("PLAY_SESSION") match {
    	  case Some(cookie) => cookie.value
    	  case None => ""
    	}
        
        play_session should be (empty)
        play_session should not include ("whatAmIdoing-authenticationToken")  
          
      }
    
  }
  "when a user is registered" should "not return authentication token if password is not valid" in {
      running(TestServer(3333)) {
    	currentTest = "registeredLoginWithInvalidPassword"
    	val fakeRequest = FakeRequest()
        val someResult = controllers.WhatAmIDoingController.registerLogin(email, password, firstName, lastName)(fakeRequest)

        val cookies = Helpers.cookies(someResult)
        val play_session = cookies.get("PLAY_SESSION") match {
    	  case Some(cookie) => cookie.value
    	  case None => ""
    	}
        
        play_session should be (empty)
        play_session should not include ("whatAmIdoing-authenticationToken")  
    	  
    }
    
  }
  
  "when a user is registered" should "return authentication token if password valid and token is valid" in {
    running(TestServer(3333)) {
    	currentTest = "registeredWithValidPasswordAndValidToken"
    	val fakeRequest = FakeRequest()
        val someResult = controllers.WhatAmIDoingController.registerLogin(email, password, firstName, lastName)(fakeRequest)

        val cookies = Helpers.cookies(someResult)
        val play_session = cookies.get("PLAY_SESSION").get.value
        
        play_session should not be empty
        play_session should include ("whatAmIdoing-authenticationToken")  
    	  
    }
  }
  
  "when a user is not registered" should
    "register the user and add authentication token to the session" in {
      running(TestServer(3333)) {
        currentTest = "registerLoginNotRegistered"

        val fakeRequest = FakeRequest()
        val someResult = controllers.WhatAmIDoingController.registerLogin(email, password, firstName, lastName)(fakeRequest)

        val cookies = Helpers.cookies(someResult)
        val play_session = cookies.get("PLAY_SESSION").get.value
        
        play_session should not be empty
        play_session should include ("whatAmIdoing-authenticationToken")
        
      }

    }
  
  "when a user is not registered" should
    "register the user and return should not store no authenticaion token if the token was not found" in {
      running(TestServer(3333)) {
        currentTest = "registerLoginNotRegisteredButInvalidToken"

        val fakeRequest = FakeRequest()
        val someResult = controllers.WhatAmIDoingController.registerLogin(email, password, firstName, lastName)(fakeRequest)

        val cookies = Helpers.cookies(someResult)
        val play_session: String = cookies.get("PLAY_SESSION") match {
          case Some(cookie) => cookie.value
          case None => ""
        }
        
        play_session should be (empty)
        }

    }

  "when user token is not valid" should "not be allowed to encoded video" in {

    running(TestServer(3333)) {

      var fakeRequest = FakeRequest()
      //  implicit val timeout = Timeout(1 seconds)
      // var som = play.api.test.Helpers.await(controllers.WhatAmIDoingController.registerLogin(email, password, firstName, lastName)(fakeRequest))
      // println(som)
      /* var result = controllers.WhatAmIDoingController.registerLogin(email, password, firstName, lastName)(fakeRequest)

      println(result)
      val cookies = Helpers.cookies(result)
      println(cookies.get("PLAY_SESSION").get.value)
     
      val value = cookies.get("PLAY_SESSION").get.value
      
      var c: AsyncHttpClient = new AsyncHttpClient()
      val r: Request = c.prepareGet("ws://localhost:9000/publishVideo?token=don").build()
      var h: WebSocketUpgradeHandler = new WebSocketUpgradeHandler.Builder().
        addWebSocketListener(
          new WebSocketTextListener() {
            @Override def onMessage(message: String) {}
            @Override def onOpen(websocket: WebSocket) {}
            @Override def onClose(websocket: WebSocket) {}
            @Override def onError(t: Throwable) {}
            @Override def onFragment(fragment: String, last: Boolean) {}
          }).build();
      var websocket: WebSocket = c.executeRequest(r, h).get()
      websocket.sendTextMessage("Beer")
      println("----ended")

       
      val json: JsValue = Json.parse("""
        		{ 
        		"response": {
        		"value" : "TOKEN NOT VALID"
        		}
        		} 
        	""")
     
       import integration.util.WebSocketClient.Messages._
       integration.util.WebSocketClient(new URI("ws://localhost:3333/publishVideo?token=don")) {
     	case Connected(client) => println("Connection has been established to: " + client.url.toASCIIString)
      case Disconnected(client, _) => println("The websocket to " + client.url.toASCIIString + " disconnected.")
      case TextMessage(client, message) => {
        println("RECV: " + message)
        client send ("ECHO: " + message)
      } 
      * 
     
    }
    *      
    */

    }

  }
}