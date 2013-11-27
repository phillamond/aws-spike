package awsspike.steps

import cucumber.api.scala.{ScalaDsl, EN}
import org.apache.http.client.methods.{HttpGet, HttpPut}
import awsspike.support.Environment
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHeader
import java.io.ByteArrayInputStream
import org.apache.http.HttpResponse

class EmailSubscriptionStepDefinitions extends ScalaDsl with EN {

  val client = Environment.httpClient
  var response: HttpResponse = null

  Given("""^a running artist email notification ReST service$"""){ () =>
    // hit ping/status endpoint
    response = client.execute(new HttpGet(Environment.baseUrl + "/status"))
    assert(response.getStatusLine.getStatusCode.equals(200))
  }

  When("""^I make a PUT request to "([^"]*)" with input body$"""){ (path:String, input:String) =>
    val httpPut = new HttpPut(Environment.baseUrl + "/status")
    val httpEntity = new BasicHttpEntity
    httpEntity.setContentType(new BasicHeader("Content-type","application/json"))
    httpEntity.setContent(new ByteArrayInputStream(input.getBytes))
    httpPut.setEntity(httpEntity)
    response = client.execute(httpPut)
  }

  Then("""^I should be returned a response status of "([^"]*)"$"""){ (status:String) =>
    assert(response.getStatusLine.equals(status))
  }

}
