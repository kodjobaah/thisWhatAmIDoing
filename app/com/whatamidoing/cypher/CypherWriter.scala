package com.whatamidoing.cypher

object CypherWriter {
  
    def createUser(fn: String, ln: String, em: String, pw_hash: String): String = {
    val s = s"""
              create (user:User {id:"$em", email:"$em", password:"$pw_hash",firstName:"$fn",lastName:"$ln"})
               """

    return s
  }

  def createToken(token: String, valid: String): String = {
    val t = s"""
                 create (token:AuthenticationToken {id:"$token" ,token:"$token",valid:"$valid"})
                """
    return t;
  }

 def createStream(stream: String): String = {
    val t = s"""
                 create (stream:Stream {id:"$stream",name:"$stream", state:"active"})
                """
    return t;
  }
 
 def linkStreamToToken(stream: String, token: String): String = {
    val t = s"""
    
    		match (a:Stream), (b:AuthenticationToken)
    		where a.name="$stream" and b.token="$token"
    		create a-[r:USING]->b
    		return r
    """
    return t
   
 }
 
 def invalidateAuthenticationTokenForUser(token: String): String = {
 
    val t = s"""
    		match (a:AuthenticationToken)
    		where a.token ="$token"
    		with a
    		match (b)-[:HAS_TOKEN]->(a)
    		with b
    		match (b)-[:HAS_TOKEN]->(c)
    		SET c.valid = "false"
    		return c as token;
    """
    return t
}
 
 def linkStreamToDay(stream: String, day: String, time: String): String = {
    val t = s"""
    			match (a:Stream), (b:Day)
    			where a.name="$stream" AND b.description="$day"
    			create a-[r:BROADCAST_ON {time:"$time"}]->b
                return r
    			"""
    return t
  }
 
  def linkUserToToken(em: String, token: String): String = {
    val linkToToken = s"""
 			  match (a:User), (b:AuthenticationToken)
			  where a.email="$em" AND b.token = "$token"
			  create a-[r:HAS_TOKEN]->b
			  return r
			  """
    return linkToToken
  }

  def associateStreamCloseToDay(stream: String, day: String, time: String): String  = {
     val linkCloseStreamToDay = s"""
     
 			  match (a:Stream), (b:Day)
			  where a.name="$stream" AND b.description = "$day"
			  create a-[r:BROADCAST_ENDED_ON {time:"$time"}]->b
			  return r
			  
			  """
    return linkCloseStreamToDay    
  }
  
  def closeStream(stream: String): String = {
    
    val res=s"""
    		match (stream:Stream)
    		where stream.name="$stream"
    		SET stream.state ="inactive"
    		return stream.state as state
    """
    return res
  }
  
  def createInvite(stream: String, email: String, id: String): String = {
    
    val res=s"""
    		match (stream:Stream), (user:User)
    		where stream.name="$stream" and user.email="$email"
    		create (invite:Invite {name:"${stream}-${email}", id:"$id"}),
    		(invite)-[r:TO_WATCH]->(stream),
    		(user)-[s:RECEIVED]->(invite)
    		return s,r
    """
    return res
    
  }

  def invalidateAllTokensForUser(email: String): String = {
    val res=s"""
        match (u:User)
        where u.email="$email"
        with u
        match (u)-[HAS_TOKEN]-(tok)
        set tok.valid = "false"
        return tok.valid as valid
      """
    return res


  }
  
  def invalidateToken(token: String) : String = {
      val res=s"""
    		match (a:AuthenticationToken)
    		where a.token = "$token"
    		SET a.valid ="false"
    		return a.valid as valid
      """
      return res
    
  }
  
  def createTokenForUser(token: String, email: String): String = {
      val res=s"""
    		match (a:User)
    		where a.email = "$email"
    		with a
    		create (token:AuthenticationToken {token:"$token",valid:"true"})
    		create a-[r:HAS_TOKEN]->token
    		return r
      """
      return res
  }
  
  def associateDayWithInvite(inviteId:String, day: String, time: String): String = {
      val res=s"""
    		match (a:Invite), (b:Day)
    		where a.id = "$inviteId" and b.description="$day"
    		with a,b
    		create a-[r:ACCEPTED_ON {time:"$time"}]->b
    		return r
      """
      return res
  }

  
  
}