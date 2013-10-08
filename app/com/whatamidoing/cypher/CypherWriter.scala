package com.whatamidoing.cypher

object CypherWriter {
  
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

}