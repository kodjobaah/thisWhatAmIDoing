package com.whatamidoing.actors.neo4j

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

class Neo4JWriter  extends Actor with ActorLogging{
	
	import Neo4JWriter._
	override def receive: Receive = {  
      case PerformOperation(operation) => {
          import models.Neo4jResult
    	  import akka.pattern.{pipe}
    	  var res: Neo4jResult = operation()
    	  sender ! WriteOperationResult(res) 
    	  
         }
	}
  
}

object Neo4JWriter {
  
  import models.Neo4jResult
  
  def props() = Props(new Neo4JWriter())

  case class PerformOperation(f: () => Neo4jResult)
  case class WriteOperationResult(val result: Neo4jResult)
}