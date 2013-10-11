package com.whatamidoing.cypher

import play.api.Logger
import org.joda.time.DateTime

object CypherWriterFunction {

  import models._
  import com.whatamidoing.actors.neo4j.Neo4JWriter._
  import org.anormcypher._

  def closeStream(stream: String): () => Neo4jResult = {
    val closeStream: Function0[Neo4jResult] = () => {
      val closeStream = Cypher(CypherWriter.closeStream(stream)).execute()

      val dt = new DateTime();
      val day = dt.getDayOfMonth();
      val time = dt.getHourOfDay() + ":" + dt.getMinuteOfDay() + ":" + dt.getSecondOfDay() + ":" + dt.getMillisOfDay()
      val dayDescription = "day " + day + " - month " + dt.getMonthOfYear() + "- year " + dt.getYear()
      val endStream = Cypher(CypherWriter.associateStreamCloseToDay(stream, dayDescription, time)).execute()

      val results: List[String] = List(closeStream.toString(), endStream.toString())
      val neo4jResult = new Neo4jResult(results)
      Logger("CypherWriterFunction.closeStream").info("results from closing stream:" + results)
      neo4jResult
    }
    closeStream
  }

  def createStream(stream: String, token: String): () => Neo4jResult = {
    val createStream: Function0[Neo4jResult] = () => {
      val createStream = Cypher(CypherWriter.createStream(stream)).execute()

      val dt = new DateTime();
      val day = dt.getDayOfMonth();
      val dayDescription = "day " + day + " - month " + dt.getMonthOfYear() + "- year " + dt.getYear()
      val time = dt.getHourOfDay() + ":" + dt.getMinuteOfDay() + ":" + dt.getSecondOfDay() + ":" + dt.getMillisOfDay()

      val linkStreamToDay = Cypher(CypherWriter.linkStreamToDay(stream, dayDescription, time)).execute()

      val linkSteamToToken = Cypher(CypherWriter.linkStreamToToken(stream, token)).execute()

      Logger("CypherWriterFunction.createStream").info("this is createStream: " + createStream)
      Logger("CypherWriterFunction.createStream").info("this is linkStream: " + linkStreamToDay)
      Logger("CypherWriterFunction.createUser").info("this is three: " + linkSteamToToken)

      val results: List[String] = List(createStream.toString(), linkStreamToDay.toString(), linkSteamToToken.toString())
      val neo4jResult = new Neo4jResult(results)
      neo4jResult

    }
    createStream

  }
  def createUser(fn: String, ln: String, em: String, p: String): () => Neo4jResult = {

    import org.mindrot.jbcrypt.BCrypt

    val createUser: Function0[Neo4jResult] = () => {
      val pw_hash = BCrypt.hashpw(p, BCrypt.gensalt())
      val newRes = Cypher(CypherWriter.createUser(fn, ln, em, pw_hash)).execute()

      val token = java.util.UUID.randomUUID.toString
      val valid = "true"

      val createToken = Cypher(CypherWriter.createToken(token, valid)).execute()
      val linkToken = Cypher(CypherWriter.linkUserToToken(em, token)).execute()

      Logger("CypherWriterFunction.createUser").info("this is one: " + newRes)
      Logger("CypherWriterFunction.createUser").info("this is two: " + createToken)
      Logger("CypherWriterFunction.createUser").info("this is three: " + linkToken)

      val results: List[String] = List(newRes.toString(), createToken.toString(), linkToken.toString())
      val neo4jResult = new Neo4jResult(results)
      neo4jResult
    }
    createUser
  }

  def createInvite(stream: String, email: String, id: String): () => Neo4jResult = {

    val createInvite: Function0[Neo4jResult] = () => {
    val createInvite = Cypher(CypherWriter.createInvite(stream, email,id)).execute()
    Logger("CypherWriterFunction.createUser").info("this is three: " + createInvite)

      val results: List[String] = List(createInvite.toString())
      val neo4jResult = new Neo4jResult(results)
      neo4jResult
    }

    createInvite
  }

}