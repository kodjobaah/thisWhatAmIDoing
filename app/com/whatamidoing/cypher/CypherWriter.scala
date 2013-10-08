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

 def createStream(stream: String): String = {
    val t = s"""
                 create (stream:Stream {name:"$stream", state="active"})
                """
    return t;
  }
 
 def linkStreamToToken(stream: String, token: String): String = {
    val t = s"""
    			match a:Stream, b:Token
    			where a.name="$stream" AND b.value="$token"
    			create a-[r:USING]->b
                
    			"""
    return t;
   
 }
 
 def linkStreamToDay(stream: String, day: Int, time: String): String = {
    val t = s"""
    			match a:Stream, b:Day
    			where a.name="$stream" AND b.value="$day"
    			create a-[r:BROADCAST_ON {time="$time"}]->b
                
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