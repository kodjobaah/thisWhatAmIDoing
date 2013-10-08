package integration.whatamidoing.cypher.suite

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.test.TestGraphDatabaseFactory
import com.whatamidoing.cypher.CypherWriter
import org.neo4j.graphdb.Transaction
import org.neo4j.tooling.GlobalGraphOperations
import com.whatamidoing.cypher.CypherInfrastructure

trait Neo4jTestDb {

  val testUser = "test@testme.com"
  val testFirstName = "testFirstName"
  val testLastName = "testLastName"
  val testPassword = "testPassword"
  val testToken = "test-Token"
  val testStream = "test-stream-id"
  val testMakeInactiveStream = "test-make-me-inactive"
  val testDay = 2
  val testTime = "12:01:00:00"
    
  val db: GraphDatabaseService =
    new TestGraphDatabaseFactory().newImpermanentDatabase()

  val getEngine = {
    val engine: ExecutionEngine = new ExecutionEngine(db)
    engine.execute(CypherWriter.createUser(testFirstName, testLastName, testUser, testPassword))
    engine.execute(CypherWriter.createToken(testToken, "true"))
    engine.execute(CypherWriter.createStream(testStream))
    engine.execute(CypherWriter.createStream(testMakeInactiveStream))
    engine.execute(CypherInfrastructure.createDay(testDay,"day"))
    engine.execute(CypherWriter.linkStreamToDay(testStream, testDay, testTime))
    engine.execute(CypherWriter.linkStreamToToken(testStream, testToken))
    engine.execute(CypherWriter.linkUserToToken(testUser, testToken))
    engine.execute(CypherWriter.associateStreamCloseToDay(testStream, testDay, testTime))
    engine
  }

}