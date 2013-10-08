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
    		  match a-[:HAS_TOKEN]->(b)
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


}