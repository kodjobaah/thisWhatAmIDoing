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
  val testStreamToClose = "test-stream-to-close"
  val testNonActiveStreamInvitedId= "test-non-active-stream-invite-id"
  val testNonActiveStreamInvitedIdEmail = "test-non-active-stream@ho.me"
  val testStreamNonActive = "test-stream-non-active"
  
    
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
    
    engine.execute(CypherWriter.createUser(testFirstName, testLastName, testInviteEmail, testPassword))
    engine.execute(CypherWriter.createInvite(testStream, testInviteEmail,testInvitedId))
    
    engine.execute(CypherWriter.createToken(testTokenToInvalidate, "true"))
   // engine.execute(CypherWriter.createTokenForUser(testNewToken, testUser))
    engine.execute(CypherWriter.associateDayWithInvite(testInvitedId, testDayDescription, testTime))
    
    engine.execute(CypherWriter.createStream(testStreamToClose))
    engine.execute(CypherWriter.closeStream(testStreamToClose))
    
    engine.execute(CypherWriter.createStream(testStreamNonActive))
    engine.execute(CypherWriter.closeStream(testStreamNonActive))
    engine.execute(CypherWriter.createUser(testFirstName, testLastName, testNonActiveStreamInvitedIdEmail, testPassword))
    engine.execute(CypherWriter.createInvite(testStreamNonActive, testInviteEmail,testNonActiveStreamInvitedId))


    engine
  }

}