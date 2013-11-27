package awsspike.steps

import cucumber.api.scala.{EN, ScalaDsl}
import cucumber.api.PendingException
import awsspike.support.Environment
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHeader
import java.io.ByteArrayInputStream

class ArtistNotificationStepDefinitions extends ScalaDsl with EN {

  val client = Environment.httpClient
  var response: HttpResponse = null

  When("""^Pink Floyd release a new track$"""){ () =>
    // no-op - just for description of scenario
  }

  When("""^a notification is POSTed to "([^"]*)" with input body$"""){ (path:String, input:String) =>
    val httpPost = new HttpPost(Environment.baseUrl + "/status")
    val httpEntity = new BasicHttpEntity
    httpEntity.setContentType(new BasicHeader("Content-type","application/json"))
    httpEntity.setContent(new ByteArrayInputStream(input.getBytes))
    httpPost.setEntity(httpEntity)
    response = client.execute(httpPost)
  }

  Then("""^the list of email subscribers are sent and email$"""){ () =>
    // TODO - consume the new event message using a test SQS queue
  }

  Then("""^the email body contains "([^"]*)"$"""){ (bodyText:String) =>
    // TODO - read the message text that will be propagated into the email body
  }

}
