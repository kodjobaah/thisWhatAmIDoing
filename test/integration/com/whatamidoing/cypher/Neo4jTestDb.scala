package integration.com.whatamidoing.cypher

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.test.TestGraphDatabaseFactory
import com.whatamidoing.cypher.CypherWriter
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
  val testUserWithInactiveToken = "test-user-with-inactive-token"
  val testUserWithInactiveTokenToken = "test-user-withn-inactive-token-token"
  val testUserFindAllInvites = "test-user-find-all-invites"
  val testUserFindAllInvitesToken = "test-user-find-all-invites-token"
  val testStreamForFindAllInvites = "test-stream-for-find-all-invites"
  val testUserFindAllInvitesInvited1 = "test-user-for-find-all-invites-one"
  val testUserFindAllInvitesInvited2 = "test-user-for-find-all-invite-two"
  val testUserFindAllInvitesInvited3 = "test-user-for-find-all-invite-three"
  val testInvitedFindAllInvitesInvitedId1 = "test-invited-for-find-all-invited-InvitedId1"
  val testInvitedFindAllInvitesInvitedId2 = "test-invited-for-find-all-invited-InvitedId2"
  val testInvitedFindAllInvitesInvitedId3 = "test-invited-for-find-all-invited-InvitedId3"
  val testInvitedFindAllInvitesInvitedId3Duplicate = "test-invited-find-all-invites-invited-3-duplicate"
  val testTokenToInvalidateOne = "test-token-to-invalidate-one"
  val testTokenToInvalidateTwo = "test-token-to-invalidate-two"
  val testTokenToInvalidateThree = "test-token-to-invalidate-three"
  val testUserForTokenToInvalidate = "test-user-for-token-to-invalidate"  
    
    
  
    
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

     engine.execute(CypherWriter.createUser(testFirstName, testLastName, testUserWithInactiveToken, testPassword))
     engine.execute(CypherWriter.createToken(testUserWithInactiveTokenToken, "false"))
     
     //Data for find all invites
     engine.execute(CypherWriter.createUser(testFirstName, testLastName, testUserFindAllInvites, testPassword))
     engine.execute(CypherWriter.createToken(testUserFindAllInvitesToken, "true"))
     engine.execute(CypherWriter.linkUserToToken(testUserFindAllInvites, testUserFindAllInvitesToken))
     engine.execute(CypherWriter.createStream(testStreamForFindAllInvites))
     engine.execute(CypherWriter.linkStreamToToken(testStreamForFindAllInvites, testUserFindAllInvitesToken))
     engine.execute(CypherWriter.createUser(testFirstName, testLastName, testUserFindAllInvitesInvited1, testPassword))
     engine.execute(CypherWriter.createInvite(testStreamForFindAllInvites, testUserFindAllInvitesInvited1,testInvitedFindAllInvitesInvitedId1))
     engine.execute(CypherWriter.createUser(testFirstName, testLastName, testUserFindAllInvitesInvited2, testPassword))
     engine.execute(CypherWriter.createInvite(testStreamForFindAllInvites, testUserFindAllInvitesInvited2,testInvitedFindAllInvitesInvitedId2))
     engine.execute(CypherWriter.createUser(testFirstName, testLastName, testUserFindAllInvitesInvited3, testPassword))
     engine.execute(CypherWriter.createInvite(testStreamForFindAllInvites, testUserFindAllInvitesInvited3,testInvitedFindAllInvitesInvitedId3))
     
     //Creating a duplicate
     engine.execute(CypherWriter.createInvite(testStreamForFindAllInvites, testUserFindAllInvitesInvited3,testInvitedFindAllInvitesInvitedId3Duplicate))
    
      engine.execute(CypherWriter.createUser(testFirstName, testLastName, testUserForTokenToInvalidate, testPassword))
      engine.execute(CypherWriter.createToken(testTokenToInvalidateOne, "false"))
      engine.execute(CypherWriter.linkUserToToken(testUserForTokenToInvalidate, testTokenToInvalidateOne))
      engine.execute(CypherWriter.createToken(testTokenToInvalidateTwo, "true"))
       engine.execute(CypherWriter.linkUserToToken(testUserForTokenToInvalidate, testTokenToInvalidateTwo))
      engine.execute(CypherWriter.createToken(testTokenToInvalidateThree, "true"))
       engine.execute(CypherWriter.linkUserToToken(testUserForTokenToInvalidate, testTokenToInvalidateThree))
    
     
     engine
  }

}