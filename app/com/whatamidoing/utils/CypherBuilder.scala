package com.whatamidoing.utils

import play.api.Logger

object CypherBuilder {

  def searchForUser(user: String): String = {
    val search = s"""
    		match a:User 
    		where a.email = "$user" 
    		return a.password as password, a.email as email
    		"""
    return search
  }

  def createUser(fn: String, ln: String, em: String, pw_hash: String): String = {
    val s = s"""
              create ($fn:User {email:"$em",password:"$pw_hash",firstName:"$fn",lastName:"$ln"})
               """

    return s
  }

  def createToken(token: String, valid: String): String = {
    val t = s"""
                 create (token:AuthenticationToken {token:"$token",valid:"$valid"})
                """
    return t;
  }

  def linkUserToToken(em: String, token: String): String = {
    val linkToToken = s"""
 			  match a:User, b:AuthenticationToken
			  where a.email="$em" AND b.token = "$token"
			  create a-[r:HAS_TOKEN]->b
			  return r
			  """
    return linkToToken
  }

  def getTokenForUser(em: String): String = {

    val res = s"""
			  start a = node:node_auto_index(email="$em")
			  match a-[:HAS_TOKEN]->(b)
			  return b.token as token , b.valid as status
	  """
    return res
  }

  import models._
  def createUserFuntion(fn: String, ln: String, em: String, p: String): () => Neo4jResult = {

    import org.mindrot.jbcrypt.BCrypt
    import com.whatamidoing.actors.neo4j.Neo4JWriter._
    import org.anormcypher._

    val createUser: Function0[Neo4jResult] = () => {
      val pw_hash = BCrypt.hashpw(p, BCrypt.gensalt())
      val newRes = Cypher(CypherBuilder.createUser(fn, ln, em, pw_hash)).execute();

      val token = java.util.UUID.randomUUID.toString
      val valid = "true"

      val createToken = Cypher(CypherBuilder.createToken(token, valid)).execute();
      val linkToken = Cypher(CypherBuilder.linkUserToToken(em, token)).execute();

      Logger("WhatAmIDoingController.registerLogin").info("this is one: " + newRes)
      Logger("WhatAmIDoingController.registerLogin").info("this is two: " + createToken)
      Logger("WhatAmIDoingController.registerLogin").info("this is three: " + linkToken)

      val results: List[String] = List(newRes.toString(), createToken.toString(), linkToken.toString())
      val neo4jResult = new Neo4jResult(results)
      neo4jResult
    }
    createUser
  }

}