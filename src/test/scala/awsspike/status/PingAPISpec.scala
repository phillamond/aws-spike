package awsspike.status

import org.scalatest.FunSpec
import org.apache.http.HttpStatus
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PingAPISpec extends FunSpec {

  describe("The ping controller API") {
    it("should respond with OK status") {
      val pingAPI = new PingAPI()
      assert(pingAPI.ping.getStatus.equals(HttpStatus.SC_OK))
    }
  }

}
