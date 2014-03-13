package com.whatamidoing.cypher

import play.Logger


object CypherReader {

  def searchForUser(user: String): String = {
    val search = s"""
    		match (a:User)
    		where a.email = "$user" 
    		return a.password as password, a.email as email
    		"""
    return search
  }

   def getTokenForUser(em: String): String = {

    val res = s"""
    		  match (a:User)
    		  where a.email = "$em"
			  with a
    		  match (a)-[:HAS_TOKEN]->(b)
    		  where b.valid = "true"
			  return b.token as token , b.valid as status
	  """
    return res
  }
  
  def getValidToken(token: String): String = {
    
    val res=s"""
    		match (token:AuthenticationToken)
    		where token.token="$token" and token.valid="true"
    		return token.token as token
      
      """
      return res
    
  }
  
  def findActiveStreamForToken(token: String) : String = {
    
    val res=s"""
    		match (a:AuthenticationToken)
    		where a.token="$token" and a.valid="true"
    		with a
    		match (a)-[r]-(b)
    		where type(r) = 'USING' and b.state='active'
    		return b.name as name
      
      """
      return res
    
  }
  
  def findStreamForInvitedId(invitedId: String) : String = {
    val res=s"""
    		match (a:Invite)
    		where a.id = "$invitedId"
    		with a
    		match (a)-[:TO_WATCH]->(r)
    		where r.state = "active"
    		return r.name as name
      """
      return res
  }
  
  def findAllInvites(token: String): String = {
        val res=s"""
          match (tok:AuthenticationToken)
          where tok.token="$token"
          with tok
          match (tok)-[:HAS_TOKEN]-(theUser)
          with theUser
          match (theUser)-[:HAS_TOKEN]-(allTokens)-[:USING]-(stream)-[TO_WATCH]-(invite)-[:RECEIVED]-(user)
          return distinct user, user.email+":"+user.firstName+":"+user.lastName as email ;
     """
      return res

  }


  def countNumberAllStreamsForDay(token: String): String = {

    val res =s"""
    match (tok:AuthenticationToken)
    where tok.token="$token"
    with tok
    match (tok)-[u:USING]-(s)
    with s
    match (s)-[si:BROADCAST_ON]-(c)
    return count(s) as count

    """
    return res
  }


  def countAllInvitesForToken(token: String): String = {
    val res =s"""
    match (tok:AuthenticationToken)
    where tok.token="$token"
    with tok
    match  (tok:AuthenticationToken)-[:USING]-(stream1:Stream)-[:TO_WATCH]-(invite:Invite)
    return count(invite) as count;


    """
    return res
     }


  def findAllInvitesForStream(token: String, displayStart: Int, displayLength: Int, sortColumn: Int, sortDirection: String, streamId: String): String = {
    val sort = sortColumn match {
      case 0 => {
        "Order by d.description "+sortDirection
      }

      case 1 => {
        "Order by a.time "+sortDirection
      }
      case 2 => {
        "Order by user.email "+sortDirection
      }
      case 3 => {
        "Order by user.firstName "+sortDirection
      }
      case 4 => {
        "Order by user.lastName "+sortDirection
      }
   }

    val skip = displayStart

    val res =s"""
    match (tok:AuthenticationToken)
    where tok.token="$token"
    with tok
    match (d)-[a?:ACCEPTED_ON]-(invite)-[:TO_WATCH]-(stream)-[:USING]-(tok)
    where stream.name="$streamId"
    with d,a,tok,stream,invite
    match (tok)-[:USING]-(stream)-[:TO_WATCH]-(invite)-[:RECEIVED]-(user)
    return d.description as day , a.time as time , user.email as email, user.firstName as firstName, user.lastName as lastName
    $sort
    SKIP $skip
    LIMIT $displayLength
    """
    return res;

  }


  def findAllStreamsForDay(token: String, displayStart: Int, displayLength: Int, sortColumn: Int, sortDirection: String): String = {

    val sort = sortColumn match {
      case 1 => {
          "Order by s.name "+sortDirection
      }

      case 2 => {
        "Order by a.description "+sortDirection
      }
      case 3 => {
        "Order by si.time "+sortDirection
      }
      case 4 => {
        "Order by d.description "+sortDirection
      }
      case 5 => {
        "Order by se.time "+sortDirection
      }
    }

    val skip = displayStart
    val res =s"""
    match (tok:AuthenticationToken)
    where tok.token="$token"
    with tok
    match (tok)-[u:USING]-(s)
    with s
    match (a:AuthenticationToken)<-[si:BROADCAST_ON]-(s)-[se?:BROADCAST_ENDED_ON]->(d)
    return s.name as stream ,a.description as day,si.time as startTime, d.description as end, se.time as endTime
    $sort
    SKIP $skip
    LIMIT $displayLength

    """
    return res
  }

  def findAllTokensForUser(email: String) : String = {
    val res =s"""
    match (u:User)
    where u.email="$email"
    with u
    match (u:User)-[HAS_TOKEN]-(tok:AuthenticationToken)-[:USING]-(stream:Stream)
    return distinct tok.token as token;

    """
    return res
  }

  def getUsersWhoHaveAcceptedToWatchStream(token: String): String = {
    val res=s"""
          match (a:AuthenticationToken) where a.token="$token" and a.valid = "true"
          with a
          match (a:AuthenticationToken)-[r:USING]-(s:Stream) where s.state = "active"
          with s
          match (s:Stream)-[TO_WATCH]-(i:Invite)-[:ACCEPTED_ON]->(d:Day)
          with i
          match (i:Invite)<-[:RECEIVED]-(u:User)
          return distinct u.email as email, u.firstName as firstName, u.lastName as lastName
    """
    Logger.info("accepted to watch ["+res+"]")
    return res
  }

  def getUsersWhoHaveBeenInvitedToWatchStream(token: String): String = {
    val res=s"""

    match (a:AuthenticationToken) where a.token="$token" and a.valid = "true"
    with a
    match (a:AuthenticationToken)-[r:USING]-(s:Stream)
    with s
    match (s:Stream)-[t:TO_WATCH]-(i:Invite)
    with i
    match (i:Invite)-[:RECEIVED]-(u:User)
    where u.email is not null
    return distinct u.email as email, u.firstName as firstName, u.lastName as lastName
    """
    Logger.info("invited to watch ["+res+"]")
    return res
  }

  def getUsersWhoHaveAcceptedToWatchStreamUsingStreamId(streamId: String): String = {
    val res=s"""
          match (s:Stream)-[:TO_WATCH]-(i:Invite)-[ACCEPTED_ON]->(d:Day)
          where s.id = "$streamId"
          with i
          match (i:Invite)<-[:RECEIVED]-(u:User)
          return distinct u.email as email, u.firstName as firstName, u.lastName as lastName
    """

    Logger.info(res);
    return res
  }

  def getUsersWhoHaveBeenInvitedToWatchStreamUsingStreamId(streamId: String): String = {
    val res=s"""
     match s-[t:TO_WATCH]-i
     where s.id = "$streamId"
     with i
     match i-[:RECEIVED]-u
     where u.email is not null
    return distinct u.email as email, u.firstName as firstName, u.lastName as lastName
    """
    Logger.info(res)
    return res
  }



  def getStreamsForCalendarThatHaveEnded(email: String,
                            startYear: Int, endYear: Int,
                            startMonth: Int, endMonth: Int,
                            startDay: Int, endDay: Int): String = {
    var res=""


    if (startYear == endYear) {
      if (startMonth == endMonth) {
        res = s"""

         match (y:Year)-[:MONTH]-(m:Month)-[:DAY]-(d:Day)-[broadcast:BROADCAST_ENDED_ON]-(s:Stream)-[:USING]-(t:AuthenticationToken)-[:HAS_TOKEN]-(u:User)
         where u.email="$email" and (y.value >= $startYear and y.value <= $endYear)
         and (m.value >= $startMonth and m.value <= $endMonth)  and (d.value >= $startDay  and d.value <= $endDay)
         return y.value as year, m.value as month, d.value as day, broadcast.time as time,s.id as streamId,u.email as email

       """

      } else {
        res = s"""
      match (y:Year)-[:MONTH]-(m:Month)-[:DAY]-(d:Day)-[broadcast:BROADCAST_ENDED_ON]-(s:Stream)-[:USING]-(t:AuthenticationToken)-[:HAS_TOKEN]-(u:User)
      where u.email="$email" and (y.value >= $startYear and y.value <= $endYear)
      and (m.value >= $startMonth and m.value <= $endMonth)
      return y.value as year, m.value as month, d.value as day, broadcast.time as time,s.id as streamId,u.email as email

       """

      }
    } else {
      res = s"""
       match (y:Year)-[:MONTH]-(m:Month)-[:DAY]-(d:Day)-[broadcast:BROADCAST_ENDED_ON]-(s:Stream)-[:USING]-(t:AuthenticationToken)-[:HAS_TOKEN]-(u:User)
       where u.email="$email" and (y.value >= $startYear and y.value <= $endYear)
       return y.value as year, m.value as month, d.value as day, broadcast.time as time,s.id as streamId,u.email as email

       """
    }
    Logger.info(res)
    return res
  }

  def getStreamsForCalendar(email: String,
                            startYear: Int, endYear: Int,
                            startMonth: Int, endMonth: Int,
                            startDay: Int, endDay: Int): String = {
    var res=""


    if (startYear == endYear) {
      if (startMonth == endMonth) {
        res = s"""

         match (y:Year)-[:MONTH]-(m:Month)-[:DAY]-(d:Day)-[broadcast:BROADCAST_ON]-(s:Stream)-[:USING]-(t:AuthenticationToken)-[:HAS_TOKEN]-(u:User)
         where u.email="$email" and (y.value >= $startYear and y.value <= $endYear)
         and (m.value >= $startMonth and m.value <= $endMonth)  and (d.value >= $startDay  and d.value <= $endDay)
         return y.value as year, m.value as month, d.value as day, broadcast.time as time,s.id as streamId,u.email as email

       """

      } else {
        res = s"""
      match (y:Year)-[:MONTH]-(m:Month)-[:DAY]-(d:Day)-[broadcast:BROADCAST_ON]-(s:Stream)-[:USING]-(t:AuthenticationToken)-[:HAS_TOKEN]-(u:User)
      where u.email="$email" and (y.value >= $startYear and y.value <= $endYear)
      and (m.value >= $startMonth and m.value <= $endMonth)
      return y.value as year, m.value as month, d.value as day, broadcast.time as time,s.id as streamId,u.email as email

       """

      }
    } else {
      res = s"""
       match (y:Year)-[:MONTH]-(m:Month)-[:DAY]-(d:Day)-[broadcast:BROADCAST_ON]-(s:Stream)-[:USING]-(t:AuthenticationToken)-[:HAS_TOKEN]-(u:User)
       where u.email="$email" and (y.value >= $startYear and y.value <= $endYear)
       return y.value as year, m.value as month, d.value as day, broadcast.time as time,s.id as streamId,u.email as email

       """
    }
    Logger.info(res)
    return res
  }

  def getEmailUsingToken(token: String): String = {

    val res=s"""

    match u-[HAS_TOKEN]-t
        where t.token="$token"
        return u.email as email
    """
    Logger.info("getEmailUsingToken["+res+"]")
    return res

  }

  def checkToSeeIfCheckPasswordIdIsValid(cpId: String): String = {

    val res = s"""

        match (cp:ChangePassword) 
        where cp.id = "$cpId" and cp.state="active"
    	return cp.state as state;
    """
     Logger.info("--checkToSeeIfCheckPasswordIdIsValid["+res+"]")
     return res

  }

 def fetchUserDetails(token: String): String = {

     val res= s"""

     match (a)-[HAS_TOKEN]-(b)
     where b.valid="true" and b.token="$token"
     return a.email as email, a.firstName as firstName , a.lastName as lastName
     """
     Logger.info("--fecthUserDetails["+res+"]")
     return res;
  }




}