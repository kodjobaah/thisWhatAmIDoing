package com.whatamidoing.actors.neo4j

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

class Neo4JReader  extends Actor with ActorLogging{
	
	import Neo4JReader._
	override def receive: Receive = {  
      case PerformReadOperation(operation) => {
          import models.Neo4jResult
    	  import akka.pattern.{pipe}
    	  var res: Neo4jResult = operation()
    	  sender ! ReadOperationResult(res) 
    	  
         }
	}
  
}

object Neo4JReader {
  
  import models.Neo4jResult
  
  def props() = Props(new Neo4JReader())

  case class PerformReadOperation(f: () => Neo4jResult)
  case class ReadOperationResult(val result: Neo4jResult)
}