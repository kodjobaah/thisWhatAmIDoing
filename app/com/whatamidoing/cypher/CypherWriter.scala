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
                 create (stream:Stream {name:"$stream", state:"active"})
                """
    return t;
  }
 
 def linkStreamToToken(stream: String, token: String): String = {
    val t = s"""
    
    		match a:Stream, b:AuthenticationToken
    		where a.name="$stream" and b.token="$token"
    		create a-[r:USING]->b
    		return r
    """
    return t
   
 }
 
 def linkStreamToDay(stream: String, day: String, time: String): String = {
    val t = s"""
    			match a:Stream, b:Day
    			where a.name="$stream" AND b.description="$day"
    			create a-[r:BROADCAST_ON {time:"$time"}]->b
                return r
    			"""
    return t
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

  def associateStreamCloseToDay(stream: String, day: String, time: String): String  = {
     val linkCloseStreamToDay = s"""
     
 			  match a:Stream, b:Day
			  where a.name="$stream" AND b.description = "$day"
			  create a-[r:BROADCAST_ENDED_ON {time:"$time"}]->b
			  return r
			  
			  """
    return linkCloseStreamToDay    
  }
  
  def closeStream(stream: String): String = {
    
    val res=s"""
    		match stream:Stream
    		where stream.name="$stream"
    		SET stream.state ="inactive"
    		return stream.state as state
    """
    return res
  }
  
  def createInvite(stream: String, email: String): String = {
    
    val res=s"""
    		match stream:Stream, a:User
    		where stream.name="$stream" and a.email="$email"
    		create (invite:Invite {name=${stream}-${email}})
    		create invite-[r:TO_WATCH]->stream
    		create a-[s:INVITED]-invite
    		return s,r
    """
    return res
    
  }
  
  
}