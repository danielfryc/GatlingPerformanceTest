import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class UserSimulation extends Simulation {

  val httpConf = http
    .baseURL("http://test_perf.reporter02.perf.aws-eu-wi.mel.elt.hosts.pearson-intl.net")

  val scn = scenario("Typical User")
    .exec(http("Create one Person")
      .post("/persons")
      .asJSON
      .body(StringBody("""{ "name": "Marcin", "age": 99 }"""))
      .check(status.is(201)))
    .pause(1)
    .exec(http("Create bulk Person")
      .post("/persons/bulk")
      .asJSON
      .body(StringBody("""[{ "name": "Marcin_A", "age": 99 }, { "name": "Marcin_B", "age": 99 }]"""))
      .check(status.is(201)))
    .pause(1)
    .exec(http("List Person page")
      .get("/persons/1")
      .check(status.is(200)))
    .pause(1)
    .exec(http("Search Person")
      .get("/persons/name/Marcin")
      .check(status.is(200)))

  setUp(scn.inject(rampUsers(1) over (10 seconds))
    .protocols(httpConf))
    .assertions(global.responseTime.max.lte(2000))  
}
