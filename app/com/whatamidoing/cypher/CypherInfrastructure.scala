package com.whatamidoing.cypher

object CypherInfrastructure {
  
  def createTime(): String = {
    val t = s"""
                 create (timeline:TimeLine {value:"timeline"})
                """
    return t;
  }
  
  def createYear(year: Int): String = {
    val t = s"""
                 create (year:Year {value:"$year"})
                """
    return t;
  }
  
  def createMonth(month: Int, description: String): String = {
    val t = s"""
                 create (month:Month {value:"$month", description: "$description"})
                """
    return t;
  }
  
   def createDay(day: Int, description: String): String = {
    val t = s"""
                 create (day:Day {value:"$day", description: "$description"})
                """
    return t;
  }
  
  
  
 def linkTimeLineWithYear(year: Int): String = {
    val linkMonthWithYear = s"""
 			  match a:TimeLine, b:year
			  where a.value="timeline" AND b.value = "$year"
			  create a-[r:YEAR]->b
			  return r
			  """
    return linkMonthWithYear
   
 }

 def linkMonthWithYear(month: Int, year: Int): String = {
    val linkMonthWithYear = s"""
 			  match a:Year, b:Month
			  where a.value="$year" AND b.value = "$month"
			  create a-[r:MONTH]->b
			  return r
			  """
    return linkMonthWithYear
   
 }

  def linkMonthToDay(month: Int, day: Int): String = {
    val linkToToken = s"""
 			  match a:Month, b:Day
			  where a.value="$month" AND b.value = "$day"
			  create a-[r:DAY]->b
			  return r
			  """
    return linkToToken
  }
  
}