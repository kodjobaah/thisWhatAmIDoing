package integration.com.whatamidoing.cypher.suite

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
  val testDay = 1
  val testTime = "12:01:00:00"
  val testDayDescription="day-description"
  val testDayToClose = "day-to-close"
  val testInviteEmail = "testInviteEmail"
  val testInvitedId = "test-invited-id"
  val testTokenToInvalidate = "test-token-to-invalidate"
  val testNewToken = "test-new-token"
    
  val db: GraphDatabaseService =
    new TestGraphDatabaseFactory().newImpermanentDatabase()

  val getEngine = {
    val engine: ExecutionEngine = new ExecutionEngine(db)
    engine.execute(CypherWriter.createUser(testFirstName, testLastName, testUser, testPassword))
    engine.execute(CypherWriter.createToken(testToken, "true"))
    engine.execute(CypherWriter.createStream(testStream))
    engine.execute(CypherWriter.createStream(testMakeInactiveStream))
    engine.execute(CypherInfrastructure.createDay(testDay,testDayDescription))
    engine.execute(CypherWriter.linkStreamToDay(testStream, testDayDescription, testTime))
    engine.execute(CypherWriter.linkStreamToToken(testStream, testToken))
    engine.execute(CypherWriter.linkUserToToken(testUser, testToken))
    engine.execute(CypherWriter.associateStreamCloseToDay(testStream, testDayToClose, testTime))
    engine.execute(CypherWriter.createInvite(testStream, testInviteEmail,testInvitedId))
    engine.execute(CypherWriter.createToken(testTokenToInvalidate, "true"))
    engine.execute(CypherWriter.createTokenForUser(testNewToken, testUser))
    engine.execute(CypherWriter.associateDayWithInvite(testInvitedId, testInvitedId, testTime))
    println("created the test data")
    engine
  }

}