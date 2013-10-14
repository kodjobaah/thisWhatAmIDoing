package com.whatamidoing.cypher

object CypherReader {

  def searchForUser(user: String): String = {
    val search = s"""
    		match a:User 
    		where a.email = "$user" 
    		return a.password as password, a.email as email
    		"""
    return search
  }

   def getTokenForUser(em: String): String = {

    val res = s"""
    		  match a:User
    		  where a.email = "$em"
			  with a
    		  match a-[:HAS_TOKEN]->b
    		  where b.valid = "true"
			  return b.token as token , b.valid as status
	  """
    return res
  }
  
  def getValidToken(token: String): String = {
    
    val res=s"""
    		match token:AuthenticationToken
    		where token.token="$token" and token.valid="true"
    		return token.token as token
      
      """
      return res
    
  }
  
  def findActiveStreamForToken(token: String) : String = {
    
    val res=s"""
    		match a:AuthenticationToken
    		where a.token="$token" and a.valid="true"
    		with a
    		match a-[r]-b
    		where type(r) = 'USING' and b.state='active'
    		return b.name as name
      
      """
      return res
    
  }
  
  def findStreamForInvitedId(invitedId: String) : String = {
    val res=s"""
    		match a:Invite
    		where a.id = "$invitedId"
    		with a
    		match a-[:TO_WATCH]->r
    		where r.state = "active"
    		return r.name as name
      """
      return res
  }
  
  def findAllInvites(token: String): String = {
        val res=s"""
    		match tok:AuthenticationToken
            where tok.token ="$token"
            with tok
            match a-[:HAS_TOKEN]-tok
            with a
    		match a-[:HAS_TOKEN]->b
    		with b
    		match b-[:USING]-c
    		with c
    		match c-[:TO_WATCH]-d
    		with d
    		match d-[:INVITED]-e
    		with distinct e
    		return e.email as email
      """
      return res

    
  }
  
  

}