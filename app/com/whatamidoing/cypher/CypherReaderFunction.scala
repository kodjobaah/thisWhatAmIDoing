package com.whatamidoing.cypher

import models.Neo4jResult
import org.anormcypher.Cypher
import play.api.Logger

object CypherReaderFunction {

  def searchForUser(em: String): () => Neo4jResult = {

    val searchForUser: Function0[Neo4jResult] = () => {
      var res = Cypher(CypherReader.searchForUser(em))
      val response = res.apply().map(row => row[String]("password")).toList
      val neo4jResult = new Neo4jResult(response)
      neo4jResult
    }
    searchForUser
  }

  def getUserToken(em: String): () => Neo4jResult = {

    val getUserToken: Function0[Neo4jResult] = () => {
      val tokens = Cypher(CypherReader.getTokenForUser(em)).apply().map(row => (row[String]("token"), row[String]("status"))).toList
      val neo4jResult = new Neo4jResult(tokens)
      Logger("CypherBuilder.getUserToken").info("this is the token: " + tokens)
      neo4jResult
    }
    getUserToken
  }

  def getValidToken(token: String): () => Neo4jResult = {
    val getValidToken: Function0[Neo4jResult] = () => {
      val tokens = Cypher(CypherReader.getValidToken(token)).apply().map(row => (row[String]("token"))).toList
      val neo4jResult = new Neo4jResult(tokens)
      Logger("CypherBuilder.getUserToken").info("this is a valid token: " + tokens)
      neo4jResult
    }
    getValidToken

  }
  
  def findActiveStreamForToken(token: String): () => Neo4jResult = {
     val findActiveStream: Function0[Neo4jResult] = () => {
       val name = Cypher(CypherReader.findActiveStreamForToken(token)).apply().map(row => (row[String]("name"))).toList
       val neo4jResult = new Neo4jResult(name)
       Logger("CypherBuiler.findActiveStreamForToken").info("name of active strem:"+name)
       neo4jResult
     }
     findActiveStream
  }
  
  def findStreamForInvitedId(invitedId: String): () => Neo4jResult = {
  	val streamForInvitedId: Function0[Neo4jResult] = () => {
  	   val name = Cypher(CypherReader.findStreamForInvitedId(invitedId)).apply().map(row => (row[String]("name"))).toList  
  	   val neo4jResult = new Neo4jResult(name)
  	   Logger("CypherBuilder.findStreamForInvitedId").info("name of active stream:"+name)
  	   neo4jResult
  	}
  	streamForInvitedId
  }

}