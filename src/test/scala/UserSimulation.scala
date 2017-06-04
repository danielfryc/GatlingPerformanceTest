import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import faker._
import scala.util.Random

object User {

  val personFeed = Iterator.continually(
      Map("PersonName" -> Name.first_name, "PersonAge" -> Random.nextInt(100))
    )

  val create = repeat(10, "n") {
        feed(personFeed)
        .exec(http("Person Create")
        .post("/persons")
        .asJSON
        .body(StringBody("""{ "name": "${PersonName}", "age": ${PersonAge} }"""))
        .check(status.is(201)))
        .pause(1) //15 sec
      }

  val bulk = repeat(10, "n") {
        feed(personFeed, 10)
        .exec(http("Person Bulk")
        .post("/persons/bulk")
        .asJSON
        .body(StringBody("""[""" + (1 to 10).map{ i => """{ "name": "${PersonName"""+i+"""}", "age": ${PersonAge"""+i+"""} }"""}.mkString(",") + """]"""))
        .check(status.is(201)))
        .pause(1) //150 sec
      }

  val browse = repeat(5, "n") {
        exec(session => session.set("page", session.get("n").as[Int] + 1))
        .exec(http("Person List")
        .get("/persons/${page}")
        .check(status.is(200))
        .check(checkIf(session => session.get("page").as[Int] == 1) {
            jsonPath("$..name").findAll.saveAs("names")
          }
        ))
        .pause(1) //60 sec
      }

  val search = repeat(10, "n") {
        exec(http("Person Search")
        .get("/persons/name/${names.random()}")
        .check(status.is(200)))
        .pause(1) //5 sec
      }
}

class UserSimulation extends Simulation {

  val httpConf = http
    .baseURL("http://test_perf.reporter02.perf.aws-eu-wi.mel.elt.hosts.pearson-intl.net")

  val userScenario = scenario("Typical User")
    .exec(User.create, User.bulk, User.browse, User.search)

  setUp(userScenario.inject(rampUsers(1) over (10 seconds))
    .protocols(httpConf))
    .assertions(global.responseTime.max.lte(2000))  
}
