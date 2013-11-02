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
  
  def findAllInvites(email: String): () => Neo4jResult = {
  	val findAllInvites: Function0[Neo4jResult] = () => {
  	   val allInvites = Cypher(CypherReader.findAllInvites(email)).apply().map(row => (row[String]("email"))).toList  
  	   val neo4jResult = new Neo4jResult(allInvites)
  	   Logger("CypherBuilder.findAllInvites").info("all invites:"+allInvites)
  	   neo4jResult
  	}
  	findAllInvites
  }

  def findAllStreamsForDay(token: String, displayStart: Int, displayLength: Int, sortColumn: Int, sortDirection: String): () => Neo4jResult = {
    val findAllStreamsForDay: Function0[Neo4jResult] = () => {
      val allStreams = Cypher(CypherReader.findAllStreamsForDay(token,displayStart, displayLength,sortColumn,sortDirection)).apply().map(row => (row[String]("stream"),row[String]("day"),row[String]("startTime"),row[Option[String]]("end"),row[Option[String]]("endTime"))).toList
      val neo4jResult = new Neo4jResult(allStreams)
     // Logger("CypherBuilder.findAllStreamsForDay").info("all streams:"+allStreams)
      neo4jResult
    }
    findAllStreamsForDay
  }

  def findAllInvitesForStream(token: String, displayStart: Int, displayLength: Int, sortColumn: Int, sortDirection: String, streamId: String): () => Neo4jResult = {
    val findAllInvitesForStream: Function0[Neo4jResult] = () => {
      val allStreams = Cypher(CypherReader.findAllInvitesForStream(token,displayStart, displayLength,sortColumn,sortDirection,streamId)).apply().map(row => (row[Option[String]]("day"),row[Option[String]]("time"),row[String]("email"),row[Option[String]]("firstName"),row[Option[String]]("lastName"))).toList
      val neo4jResult = new Neo4jResult(allStreams)
      // Logger("CypherBuilder.findAllStreamsForDay").info("all streams:"+allStreams)
      neo4jResult
    }
    findAllInvitesForStream
  }



  def countNumberAllStreamsForDay(token: String): () => Neo4jResult = {
    val countNumberAllStreamsForDay: Function0[Neo4jResult] = () => {
      val allStreams = Cypher(CypherReader.countNumberAllStreamsForDay(token)).apply().map(row => (row[Int]("count"))).toList
      val neo4jResult = new Neo4jResult(List(allStreams.head.toString))
     // Logger("CypherBuilder.countNumberAllStreamsForDay").info("numbers streams:"+allStreams)
      neo4jResult
    }
    countNumberAllStreamsForDay
  }

  def countAllInvitesForToken(token: String): () => Neo4jResult = {
    val countAllInvitesForToken: Function0[Neo4jResult] = () => {
      val allStreams = Cypher(CypherReader.countAllInvitesForToken(token)).apply().map(row => (row[Int]("count"))).toList
      val neo4jResult = new Neo4jResult(List(allStreams.head.toString))
      // Logger("CypherBuilder.countNumberAllStreamsForDay").info("numbers streams:"+allStreams)
      neo4jResult
    }
    countAllInvitesForToken
  }


  def findAllTokensForUser(email: String): () => Neo4jResult = {
    val findAllTokensForUser: Function0[Neo4jResult] = () => {
      val allStreams = Cypher(CypherReader.findAllTokensForUser(email)).apply().map(row => (row[Option[String]]("token"))).toList
      val neo4jResult = new Neo4jResult(allStreams)
      // Logger("CypherBuilder.findAllStreamsForDay").info("all streams:"+allStreams)
      neo4jResult
    }
    findAllTokensForUser
  }



}