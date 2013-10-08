package integration.com.whatamidoing.cypher

import org.scalatest.FlatSpec
import integration.whatamidoing.cypher.suite.Neo4jTestDb
import com.whatamidoing.cypher.CypherReader
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfter
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.Transaction
import org.neo4j.tooling.GlobalGraphOperations
import com.whatamidoing.cypher.CypherWriter

class CypherReaderSpec extends FlatSpec with Neo4jTestDb with Matchers with BeforeAndAfter {
  
  "Given a valid user" should "return the token" in {
	  var result = getEngine.execute(CypherReader.getTokenForUser(testUser))
	  result.columns().size should equal(2)
	  var token = ""
	  var status = ""
	  val it = result.iterator()
	  while(it.hasNext()) {
	    val res = it.next()
	     token = res.get("token").asInstanceOf[String]
	     status = res.get("status").asInstanceOf[String]
	  }
	  
	  token should equal (testToken)
	  status should equal ("true")
  }
  
  "Given a valid token" should "return the token since its valid" in {
    var result = getEngine.execute(CypherReader.getValidToken(testToken))
	  result.columns().size should equal(1)
	  var token = ""
	  val it = result.iterator()
	  while(it.hasNext()) {
	    val res = it.next()
	     token = res.get("token").asInstanceOf[String]
	  }
	  
	  token should equal (testToken)
  }
  
   "Given an valid token" should "return nothing" in {
    var result = getEngine.execute(CypherReader.getValidToken("test-invalid-token"))
    result.iterator().hasNext() should equal (false)
  }
   
   "Given a active stream" should "turn inactive when the stream is close" in {
     var result = getEngine.execute(CypherWriter.closeStream(testMakeInactiveStream))
     var resp = ""
     val it = result.iterator()
     while(it.hasNext()) {
        val res = it.next()
	     resp = res.get("state").asInstanceOf[String]
     }
     
     resp should equal ("inactive")
   }
   
   "given the token" should "return the name of the active stream " in {
     var result = getEngine.execute(CypherReader.findActiveStreamForToken(testToken))
     var res = ""
     val it = result.iterator()
     while(it.hasNext()) {
       val resp = it.next()
       res = resp.get("name").asInstanceOf[String]
       
     }
     println(res)
     res should equal(testStream)
     
     
   }

}