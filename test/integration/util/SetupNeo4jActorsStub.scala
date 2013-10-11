package integration.util

import akka.actor.Actor
import org.scalatest.Suite
import org.scalatest.BeforeAndAfterEach
import org.mindrot.jbcrypt.BCrypt
import org.scalamock.scalatest.MockFactory
import models.Neo4jResult
import akka.pattern.{ pipe }
import controllers.WhatAmIDoingController

trait SetupNeo4jActorsStub extends BeforeAndAfterEach  { this: Suite =>

  val email = Option("testEmail@hotmail.com")
  val password = Option("testpassword")
  val firstName = Option("firstName")
  val lastName = Option("lastName")

  var currentTest = "NOTHING"

  override def beforeEach() {

    import akka.testkit.TestActorRef
    var numberOfTimesCalled = 0
    import com.whatamidoing.actors.neo4j.Neo4JReader._
    import com.whatamidoing.actors.neo4j.Neo4JWriter._
    
    implicit var actorSystem = akka.actor.ActorSystem("WhatAmIdoingControllerSpec")
    
    
    val neo4jWriter = TestActorRef(new Actor {
    	def receive = {
    	   case PerformOperation(operation) => {
    	    var result = List(("true"))
            val res = Neo4jResult(result)
    	    sender ! WriteOperationResult(res) 
    	   }
    	       
    	}
    })
    
    
    val neo4jReader = TestActorRef(new Actor {
      def receive = {
        case PerformReadOperation(operation) => {

          var res: Neo4jResult = Neo4jResult(List(""))
          println("------------operation received:"+operation)
          if (currentTest.equalsIgnoreCase("whatAmIdoingViewPage")) {
            println("---- INSIDE ----THIS---")
             if (numberOfTimesCalled == 0) {
              var result = List("testPageToView.flv")
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            }
          } else if (currentTest.equalsIgnoreCase("invitedToViewTokenNotValid")) {
        	  if (numberOfTimesCalled == 0) {
              var result = List()
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            } 
          }else if (currentTest.equalsIgnoreCase("invitedToViewStreamButDoesNotExist")) {
        	  if (numberOfTimesCalled == 0) {
              var result = List("ValidToken")
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            } else if (numberOfTimesCalled == 1) {
              var result = List((""))
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            } 
          }else if (currentTest.equalsIgnoreCase("invitedToJoinAndRegistered")) {
        	  if (numberOfTimesCalled == 0) {
              var result = List("ValidToken")
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            } else if (numberOfTimesCalled == 1) {
              var result = List(("stream name"))
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            } else if (numberOfTimesCalled == 2) {
              var result = List((""))
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            }
          } else if (currentTest.equalsIgnoreCase("registeredLoginWithValidPasswordInvalidToken")) {
             if (numberOfTimesCalled == 0) {
              val pw_hash = BCrypt.hashpw(password.get, BCrypt.gensalt())
              var result = List(pw_hash)
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            } else if (numberOfTimesCalled == 1) {
              var result = List(("test-token", "false"))
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            } 
            
          } else if (currentTest.equalsIgnoreCase("registeredLoginWithInvalidPassword")) {
            if (numberOfTimesCalled == 0) {
              var result = List("invalid hash")
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            } 
            
          } else if (currentTest.equalsIgnoreCase("registeredWithValidPasswordAndValidToken")) {
          
            if (numberOfTimesCalled == 0) {
              val pw_hash = BCrypt.hashpw(password.get, BCrypt.gensalt())
              var result = List(pw_hash)
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            } else if (numberOfTimesCalled == 1) {
              var result = List(("test-token", "true"))
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            }
            
          } else if (currentTest.equalsIgnoreCase("registerLoginNotRegisteredButInvalidToken")) {
          
            if (numberOfTimesCalled == 0) {
            	var result = List()
            	res = Neo4jResult(result)
            	numberOfTimesCalled = numberOfTimesCalled + 1
            } else if (numberOfTimesCalled == 1) {
              var result = List(("test-token", "false"))
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            }
            
          } else if (currentTest.equalsIgnoreCase("registerLoginNotRegistered")) {
            if (numberOfTimesCalled == 0) {
            	var result = List()
            	res = Neo4jResult(result)
            	numberOfTimesCalled = numberOfTimesCalled + 1
            } else if (numberOfTimesCalled == 1) {
              var result = List(("test-token", "true"))
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            }
            
          } else {
            if (numberOfTimesCalled == 0) {
              val pw_hash = BCrypt.hashpw(password.get, BCrypt.gensalt())
              var result = List(pw_hash)
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1

            } else if (numberOfTimesCalled == 1) {
              var result = List(("test-token", "true"))
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1

            } else {
              var result = List("test-token")
              res = Neo4jResult(result)
              numberOfTimesCalled = numberOfTimesCalled + 1
            }
          }
          sender ! ReadOperationResult(res)

        }
      }
    })

    import com.whatamidoing.actors.red5.FrameSupervisor._

    val frameSupervisor = TestActorRef(new Actor {
      def receive = {
        case RTMPMessage(message, token) => {
          println(message)
        }
      }
    })

    controllers.WhatAmIDoingController.neo4jreader = neo4jReader
    controllers.WhatAmIDoingController.frameSupervisor = frameSupervisor
    controllers.WhatAmIDoingController.neo4jwriter = neo4jWriter

    super.beforeEach() // To be stackable, must call super.beforeEach
  }

  override def afterEach() {
    try super.afterEach() // To be stackable, must call super.afterEach
  }
}

