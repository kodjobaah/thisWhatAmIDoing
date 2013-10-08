package com.whatamidoing.cypher

import play.api.Logger

object CypherWriterFunction {

  import models._
  import com.whatamidoing.actors.neo4j.Neo4JWriter._
  import org.anormcypher._

  def createUser(fn: String, ln: String, em: String, p: String): () => Neo4jResult = {

    import org.mindrot.jbcrypt.BCrypt

    val createUser: Function0[Neo4jResult] = () => {
      val pw_hash = BCrypt.hashpw(p, BCrypt.gensalt())
      val newRes = Cypher(CypherWriter.createUser(fn, ln, em, pw_hash)).execute();

      val token = java.util.UUID.randomUUID.toString
      val valid = "true"

      val createToken = Cypher(CypherWriter.createToken(token, valid)).execute();
      val linkToken = Cypher(CypherWriter.linkUserToToken(em, token)).execute();

      Logger("CypherWriterFunction.createUser").info("this is one: " + newRes)
      Logger("CypherWriterFunction.createUser").info("this is two: " + createToken)
      Logger("CypherWriterFunction.createUser").info("this is three: " + linkToken)

      val results: List[String] = List(newRes.toString(), createToken.toString(), linkToken.toString())
      val neo4jResult = new Neo4jResult(results)
      neo4jResult
    }
    createUser
  }

}