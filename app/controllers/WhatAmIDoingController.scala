package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent._
import scala.concurrent.future
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.pattern.AskTimeoutException
import akka.util.Timeout

import com.whatamidoing.actors.red5._
import com.whatamidoing.actors.neo4j._

object WhatAmIDoingController extends Controller {

  //Used by ?(ask)
  implicit val timeout = Timeout(1 seconds)

  val system = ActorSystem("whatamidoing-system")

  //NOTE: Should we just be passing one database access service..or should each actor get a copy of their own
  val rtmpSender = system.actorOf(RTMPSender.props("hey"), "rtmpsender")
  val neo4jwriter = system.actorOf(Neo4JWriter.props(), "neo-4j-writer-supervisor")
  val neo4jreader = system.actorOf(Neo4JReader.props(), "neo-4j-reader-supervisor")

  def whatAmIdoing = Action { implicit request =>
    Ok(views.html.whatamidoing())

  }

  def invite(email: String) = Action { implicit request =>

    import com.whatamidoing.mail._
    import com.whatamidoing.mail.mailer._

    send a new Mail(
      from = ("kodjobaah@gmail.com", "What Am I doing!!"),
      to = email,
      subject = "What Am I Doing",
      message = "Click on the link http://5.79.24.141:9000/whatamidoing ")
    Logger("WhatAmIDoingController.invite").info("sending email to =:" + email)

    Ok("done")
  }

  import ExecutionContext.Implicits.global
  def registerLogin(email: Option[String], password: Option[String], firstName: Option[String], lastName: Option[String]) =
    Action.async { implicit request =>

      import org.anormcypher._
      import com.whatamidoing.utils.CypherBuilder

      val em = email.get
      val p = password.get
      val fn = firstName.get
      val ln = lastName.get

      val searchForUser = CypherBuilder.searchForUserFunction(em)

      import com.whatamidoing.actors.neo4j.Neo4JReader._
      val response: Future[Any] = ask(neo4jreader, PerformReadOperation(searchForUser)).mapTo[Any]

      var results: scala.concurrent.Future[play.api.mvc.SimpleResult] = response.map(
        {
          case ReadOperationResult(readResults) => {

            import org.mindrot.jbcrypt.BCrypt
            var stuff = "Not Logged In"
            if (readResults.results.size < 1) {

              import com.whatamidoing.actors.neo4j.Neo4JWriter._
              val createUser = CypherBuilder.createUserFuntion(fn, ln, em, p);

              val readResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createUser)).mapTo[Any]

              var readResult: scala.concurrent.Future[play.api.mvc.SimpleResult] = readResponse.map(
                {
                  case WriteOperationResult(results) => {
                    Ok(results.results.toString())
                  }
                })

              readResult
            } else {

              val dbhash = readResults.results.head
              if (BCrypt.checkpw(p, dbhash)) {
                val tokens = Cypher(CypherBuilder.getTokenForUser(em)).apply().map(row => (row[String]("token"), row[String]("status"))).toList
                val tok = tokens.head
                Logger("WhatAmIDoingController.registerLogin").info("this is the token: " + tok)
                if (tok._2 == "true") {
                  Logger("WhatAmIDoingController.registerLogin").info("adding to cookie: " + tok._1)
                  import play.api.mvc.Cookie
                  future(
                    Ok("DID THE STUFF").withSession(
                      "whatAmIdoing-authenticationToken" -> tok._1))
                } else {
                  future(Ok("TOKEN NOT VALID"))
                }
              } else {
                stuff = "Wrong Password"
                future(Ok(stuff))
              }
            }
            Ok(readResults.results.toString())
          }
        })
        results
    }

  import play.api.mvc.WebSocket
  import play.api.libs.iteratee.Iteratee
  import play.api.libs.iteratee.Enumerator
  import play.api.libs.concurrent.Execution.Implicits._
  import scala.concurrent.Future
  var v = 0
  def publishVideo(username: String) = WebSocket.async[String] { request =>

    // Log events to the console
    v = v + 1

    import com.whatamidoing.actors.red5.RTMPSender._
    val in = Iteratee.foreach[String](s => {
      //Logger("MyApp").info("Log established %d".format(username.length()))
      rtmpSender ! RTMPMessage(s)

    }).map { _ =>
      println("Disconnected")
    }

    // Send a single 'Hello!' message
    val out = Enumerator("Hello!")

    Future((in, out))
  }

}
