package integration.whatamidoing.cypher.suite
import org.scalatest._
import integration.com.whatamidoing.cypher.CypherReaderSpec
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.test.TestGraphDatabaseFactory

class RunAllCypherTestSpec extends Suites(
  new CypherReaderSpec
 
) with BeforeAndAfterAll  {
 
    // Set up the embeded database
	override def beforeAll() {
    
  }

  // Clearing down the databasew
  override def afterAll() {
     
  }

}