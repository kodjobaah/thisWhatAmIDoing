package com.whatamidoing.utils

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
  
  def getTokenForUser(em: String): String  = {
    
	  val res =s"""
			  start a = node:node_auto_index(email="$em")
			  match a-[:HAS_TOKEN]->(b)
			  return b.token as token , b.valid as status
	  """
	  return res
  }

}