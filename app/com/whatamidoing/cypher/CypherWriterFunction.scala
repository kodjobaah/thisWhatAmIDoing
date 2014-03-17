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
      val time = dt.getHourOfDay() + ":" + dt.getMinuteOfHour + ":" + dt.getSecondOfMinute
      val dayDescription = "day " + day + " - month " + dt.getMonthOfYear() + "- year " + dt.getYear()
      val endStream = Cypher(CypherWriter.associateStreamCloseToDay(stream, dayDescription, time)).execute()

      val results: List[String] = List(closeStream.toString(), endStream.toString())
      val neo4jResult = new Neo4jResult(results)
      Logger("CypherWriterFunction.closeStream").info("results from closing stream:" + results)
      neo4jResult
    }
    closeStream
  }

  def createTestStream2012(stream: String, token: String): () => Neo4jResult = {
    val createStream: Function0[Neo4jResult] = () => {
      val createStream = Cypher(CypherWriter.createStream(stream)).execute()

      val dt = new DateTime();
      val day = dt.getDayOfMonth();
      val dayDescription = "day " + day + " - month " + dt.getMonthOfYear() + "- year " + 2012
      val time = dt.getHourOfDay() + ":" + dt.getMinuteOfHour + ":" + dt.getSecondOfMinute

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

  def createStream(stream: String, token: String): () => Neo4jResult = {
    val createStream: Function0[Neo4jResult] = () => {
      val createStream = Cypher(CypherWriter.createStream(stream)).execute()

      val dt = new DateTime();
      val day = dt.getDayOfMonth();
      val dayDescription = "day " + day + " - month " + dt.getMonthOfYear() + "- year " + dt.getYear()
      val time = dt.getHourOfDay + ":" + dt.getMinuteOfHour + ":" + dt.getSecondOfMinute

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

      val createInvite = Cypher(CypherWriter.createInvite(stream, email, id)).execute()
      Logger("CypherWriterFunction.createUser").info("this is createinvite: " + createInvite)

      val results: List[String] = List(createInvite.toString())
      val neo4jResult = new Neo4jResult(results)
      neo4jResult
    }

    createInvite
  }

  def invalidateToken(token: String): () => Neo4jResult = {

    val invalidate: Function0[Neo4jResult] = () => {
      val invalidate = Cypher(CypherWriter.invalidateToken(token)).execute()
      Logger("CypherWriterFunction.invalidateToken").info("this is invalidateToken: " + invalidate)

      val results: List[String] = List(invalidate.toString())
      val neo4jResult = new Neo4jResult(results)
      neo4jResult
    }

    invalidate
  }


  def invalidateAllTokensForUser(email: String): () => Neo4jResult = {

    val invalidate: Function0[Neo4jResult] = () => {
      val invalidate = Cypher(CypherWriter.invalidateAllTokensForUser(email)).execute()
      Logger("CypherWriterFunction.invalidateAllTokensForUser").info("this is invalidateToken: " + invalidate)

      val results: List[String] = List(invalidate.toString())
      val neo4jResult = new Neo4jResult(results)
      neo4jResult
    }

    invalidate
  }

  
  def createTokenForUser(token: String, email: String): () => Neo4jResult = {
    val createTokenForUser: Function0[Neo4jResult] = () => {
      val createTokenForUser = Cypher(CypherWriter.createTokenForUser(token, email)).execute()
      Logger("CypherWriterFunction.createTokenForUser").info("this is createTokenForUser: " + createTokenForUser)

      val neo4jResult = new Neo4jResult(List(createTokenForUser.toString()))
      neo4jResult
    }

    createTokenForUser
  }

  def associateDayWithInvite(inviteId: String): () => Neo4jResult = {
    
    val associatedDayWithInvite: Function0[Neo4jResult] = () => {
      
      val dt = new DateTime();
      val day = dt.getDayOfMonth();
      val dayDescription = "day " + day + " - month " + dt.getMonthOfYear() + "- year " + dt.getYear()
      val time = dt.getHourOfDay() + ":" + dt.getMinuteOfHour + ":" + dt.getSecondOfMinute

      val assocaiteDayWithInvited = Cypher(CypherWriter.associateDayWithInvite(inviteId, dayDescription, time)).execute()
      Logger("CypherWriterFunction.assocaiteDayWithInvited").info("this is associateDayWithInvited: " + assocaiteDayWithInvited)

      val neo4jResult = new Neo4jResult(List(assocaiteDayWithInvited.toString()))
      neo4jResult
    }

    associatedDayWithInvite
  }

  def changePasswordRequest(email:String,changePasswordId: String): () => Neo4jResult = {
    
    val changePasswordRequest: Function0[Neo4jResult] = () => {
      
      val dt = new DateTime();
      val day = dt.getDayOfMonth();
      val dayDescription = "day " + day + " - month " + dt.getMonthOfYear() + "- year " + dt.getYear()
      val time = dt.getHourOfDay() + ":" + dt.getMinuteOfHour + ":" + dt.getSecondOfMinute

      val changePasswordRequest = Cypher(CypherWriter.changePasswordRequest(email, dayDescription, time,changePasswordId)).execute()
      Logger("CypherWriterFunction.changePasswordRequest").info("this is changedPasswordRequest: " + changePasswordRequest)

      val neo4jResult = new Neo4jResult(List(changePasswordRequest.toString()))
      neo4jResult
    }
    changePasswordRequest
  }


  def updatePassword(cpId: String,newPassword: String): () => Neo4jResult = {
    
    val updatePassword: Function0[Neo4jResult] = () => {
      
      val dt = new DateTime();
      val day = dt.getDayOfMonth();
      val dayDescription = "day " + day + " - month " + dt.getMonthOfYear() + "- year " + dt.getYear()
      val time = dt.getHourOfDay() + ":" + dt.getMinuteOfHour + ":" + dt.getSecondOfMinute

      val updatePassword = Cypher(CypherWriter.updatePassword(cpId, dayDescription, newPassword,time)).execute()
      Logger("CypherWriterFunction.updatePassword").info("this is updatedPassword: " + updatePassword)

      val neo4jResult = new Neo4jResult(List(updatePassword.toString()))
      neo4jResult
    }
    updatePassword
  }


  def deactivatePreviousChangePasswordRequest(email: String) : () => Neo4jResult = {
    
    val deactivatePreviousChangePasswordRequest: Function0[Neo4jResult] = () => {
      
      val deactivatePreviousChangePasswordRequest = Cypher(CypherWriter.deactivatePreviousChangePasswordRequest(email)).execute()
      Logger("CypherWriterFunction.deactivatePreviousChangePasswordRequest").info("this is deactivatePreviousChangePasswordRequest: " + deactivatePreviousChangePasswordRequest)

      val neo4jResult = new Neo4jResult(List(deactivatePreviousChangePasswordRequest.toString()))
      neo4jResult
    }
    deactivatePreviousChangePasswordRequest
  }

  def updateUserDetails(token:String, firstName: String, lastName: String) : () => Neo4jResult = {

    val updateUserDetails: Function0[Neo4jResult] = () => {
      val updateUserDetails = Cypher(CypherWriter.updateUserDetails(token,firstName,lastName)).execute()
      Logger("CypherWriterFunction.updateuserdetails").info("this is checkToSeeIfCheckPasswordIdIsValid: " + updateUserDetails)
      val neo4jResult = new Neo4jResult(List(updateUserDetails.toString()))
      neo4jResult
    }
    updateUserDetails
  }

  def createLocationForStream(token: String, latitude: Double, longitude: Double): () => Neo4jResult = {

    val createLocationForStream: Function0[Neo4jResult] = () => {
      val createLocationForStream = Cypher(CypherWriter.createLocationForStream(token,latitude,longitude)).execute()
      Logger("CypherWriterFunction.createLocationForStream").info("this is createLocationForStream: " + createLocationForStream)
      val neo4jResult = new Neo4jResult(List(createLocationForStream.toString()))
      neo4jResult
    }
    createLocationForStream
  }


}