package com.whatamidoing.cypher

import play.Logger

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
    return t
  }

 def createStream(stream: String): String = {
    val t = s"""
                 create (stream:Stream {id:"$stream",name:"$stream", state:"active"})
                """
    return t
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


  def createChangePassword(id: String) : String = {
      val t = s"""
	   create (changePassword:ChangePassword {id:"$id",state:"active"})
	   """
     return t
  }

  def changePasswordRequest(email:String, day: String, time: String, changePasswordId: String): String = {

     val res=s"""
     	 match (u:User), (d:Day)
	 where u.email = "$email" and d.description="$day"
	 with u,d
	 create (cp:ChangePassword {id:"$changePasswordId", state:"active"})
	 with  u,d,cp
	 create u-[r:CHANGE_PASSWORD_REQUEST]->cp-[m:MADE_ONE {time:"$time"}]->d
	 return u,d,cp
     """
     Logger.info("--change password["+res+"]")
     return res
  }

  def updatePassword(cpId: String, day:String, newPassword: String, time:String): String = {

    val res = s"""
      match (cp:ChangePassword), (day:Day)
      where cp.id = "$cpId" and day.description="$day"
      with cp, day
      match a-[s:CHANGE_PASSWORD_REQUEST]-cp
      SET cp.state = "inactive", a.password="$newPassword"
      with cp, day,a
      create cp-[c:CHANGED_ON {time:"$time"}]->day
      return a, cp,day
    """
     Logger.info("--update password["+res+"]")
    return res
  }
  

  def deactivatePreviousChangePasswordRequest(email: String): String = {

    val res = s"""
       match (a:User) 
       where a.email="$email"
       match (a)-[cp:CHANGE_PASSWORD_REQUEST]-(c)
       set c.state = "inactive"
       return a,c
    """
     Logger.info("--deactivePreviousChangePasswordRequest["+res+"]")
    return res
  }

  def updateUserDetails(token: String, firstName: String, lastName: String): String = {
      val res = s"""
      match a-[HAS_TOKEN]-t where t.token = "$token"
      set a.firstName = "$firstName", a.lastName="$lastName"
      return a;
      """
      Logger.info("--updateuserdetails["+res+"]")
      return res;

 }

  def createLocationForStream(token: String, latitude: Double, longitude: Double): String = {

      val res = s"""
      match (a:AuthenticationToken) where a.token = "$token"
      with a
      match s-[u:USING]->a
      where s.state="active"
      with s
      create (s)-[l:LOCATED_AT]->(ul:Location {latitude:$latitude,longitude:$longitude})
      return ul,l,s
      """
      Logger.info("---createLocationForStream["+res+"]")
      return res;
  }  
}