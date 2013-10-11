package integration.com.whatamidoing.cypher

import org.scalatest.FlatSpec
import integration.com.whatamidoing.cypher.suite.Neo4jTestDb
import com.whatamidoing.cypher.CypherReader
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfter
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.Transaction
import org.neo4j.tooling.GlobalGraphOperations
import com.whatamidoing.cypher.CypherWriter
import integration.com.whatamidoing.cypher.suite.Neo4jTestDb

class CypherWriterSpec extends FlatSpec with Neo4jTestDb with Matchers with BeforeAndAfter {
  
   "given the token" should "log the user out by setting the token to false" in {
     
       var result = getEngine.execute(CypherWriter.invalidateToken(testTokenToInvalidate))
	   var res = ""
       val it = result.iterator()
       while(it.hasNext()) {
        val resp = it.next()
        res = resp.get("valid").asInstanceOf[String]
       
      }
	 res should equal("false")  
   }

}