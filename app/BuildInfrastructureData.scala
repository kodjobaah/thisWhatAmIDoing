import org.anormcypher.Cypher
import com.whatamidoing.cypher.CypherInfrastructure

object BuildInfrastructureData extends App {
  
  
  
  val res19 = Cypher(CypherInfrastructure.createTimeLine()).execute()
  println("should have created timeline")
  for(day <- 1 to 31) {
    val res = Cypher(CypherInfrastructure.createDay(day, "day")).execute()
  }
  
  
  val res1 = Cypher(CypherInfrastructure.createMonth(1, "January")).execute()
  val res2 = Cypher(CypherInfrastructure.createMonth(2, "February")).execute()
  val res3 = Cypher(CypherInfrastructure.createMonth(3, "March")).execute()
  val res4 = Cypher(CypherInfrastructure.createMonth(4, "April")).execute()
  val res5 = Cypher(CypherInfrastructure.createMonth(5, "May")).execute()
  val res6 = Cypher(CypherInfrastructure.createMonth(6, "June")).execute()
  val res7 = Cypher(CypherInfrastructure.createMonth(7, "July")).execute()
  val res8 = Cypher(CypherInfrastructure.createMonth(8, "August")).execute()
  val res9 = Cypher(CypherInfrastructure.createMonth(9, "September")).execute()
  val res10 = Cypher(CypherInfrastructure.createMonth(10, "October")).execute()
  val res11 = Cypher(CypherInfrastructure.createMonth(11, "November")).execute()
  val res12 = Cypher(CypherInfrastructure.createMonth(12, "December")).execute()
  
  
  for(day <- 1 to 31) {
  
  val linkMonth1 = Cypher(CypherInfrastructure.linkMonthToDay(1, day)).execute()
  val linkMonth2 = Cypher(CypherInfrastructure.linkMonthToDay(2, day)).execute()
  val linkMonth3 = Cypher(CypherInfrastructure.linkMonthToDay(3, day)).execute()
  val linkMonth4 = Cypher(CypherInfrastructure.linkMonthToDay(4, day)).execute()
  val linkMonth5 = Cypher(CypherInfrastructure.linkMonthToDay(5, day)).execute()
  val linkMonth6 = Cypher(CypherInfrastructure.linkMonthToDay(6, day)).execute()
  val linkMonth7 = Cypher(CypherInfrastructure.linkMonthToDay(7, day)).execute()
  val linkMonth8 = Cypher(CypherInfrastructure.linkMonthToDay(8, day)).execute()
  val linkMonth9 = Cypher(CypherInfrastructure.linkMonthToDay(9, day)).execute()
  val linkMonth10 = Cypher(CypherInfrastructure.linkMonthToDay(10, day)).execute()
  val linkMonth11 = Cypher(CypherInfrastructure.linkMonthToDay(11, day)).execute()
  val linkMonth12 = Cypher(CypherInfrastructure.linkMonthToDay(12, day)).execute()
  
  } 
  
  val res13 = Cypher(CypherInfrastructure.createYear(2013)).execute() 
  val res14 = Cypher(CypherInfrastructure.createYear(2014)).execute() 
  val res15 = Cypher(CypherInfrastructure.createYear(2015)).execute() 
  
  for(month <- 1 to 12) {
	  val res14 = Cypher(CypherInfrastructure.linkMonthWithYear(month,2013)).execute()
	  val res15 = Cypher(CypherInfrastructure.linkMonthWithYear(month,2014)).execute()
	  val res16 = Cypher(CypherInfrastructure.linkMonthWithYear(month,2015)).execute()
  }
  
  val res16 = Cypher(CypherInfrastructure.linkTimeLineWithYear(2013)).execute()
  val res17 = Cypher(CypherInfrastructure.linkTimeLineWithYear(2014)).execute()
  val res18 = Cypher(CypherInfrastructure.linkTimeLineWithYear(2015)).execute()
  
  
  
  println("done")
  
  
}