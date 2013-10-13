package models

import models.Neo4jResult
import play.api.libs.json.JsValue

object Messages {
  
    case class PerformReadOperation(f: () => Neo4jResult)
    case class ReadOperationResult(val result: Neo4jResult)

    case class PerformOperation(f: () => Neo4jResult)
    case class WriteOperationResult(val result: Neo4jResult)
    
    case class RTMPMessage(val message: JsValue, val token: String)
    case class StopVideo(val token: String)
    
    case class EncodeFrame(frame: String)
    case class EndTransmission()

}