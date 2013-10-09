import org.anormcypher.Cypher
import com.whatamidoing.cypher.CypherInfrastructure

object BuildInfrastructureData extends App {
  
  
  
  val res19 = Cypher(CypherInfrastructure.createTimeLine()).execute()
  println("should have created timeline")
  val months: List[String] = List("January","February","March","April","May","June","July","August","September","October","November","December")
  val years: List[Int] = List(2012,2013,2014,2015,2016)

 for(year <- 0 to 4 ) {
	val yearNode = Cypher(CypherInfrastructure.createYear(years(year))).execute()
	val yearTimeLine = Cypher(CypherInfrastructure.linkTimeLineWithYear(years(year))).execute()   
  } 

 for(month <-0 to 11) {
        println("month:"+month)
        val actualMonth = month + 1;
     
	 	var monthDescription = months(month)+" - "+years(0)  
        val monthNode = Cypher(CypherInfrastructure.createMonth(month+1,monthDescription)).execute() 
	    val yearMonth = Cypher(CypherInfrastructure.linkMonthWithYear(monthDescription,years(1))).execute()
      //Associating a days to a month
      for(day <- 0 to 30) {
          println("day:"+day)
          val actualDay = day + 1;
	      var dayDescription =  "day "+actualDay+" - month "+actualMonth + "- year"+1
	      val dayNode = Cypher(CypherInfrastructure.createDay(actualDay,dayDescription)).execute()
		  val monthToDay = Cypher(CypherInfrastructure.linkMonthToDay(monthDescription,dayDescription)).execute()
	   }
	 
 }   
  
 
 
  
  val res17 = Cypher(CypherInfrastructure.linkTimeLineWithYear(2014)).execute()
  val res18 = Cypher(CypherInfrastructure.linkTimeLineWithYear(2015)).execute()
  
  
  
  println("done")
  
  
}