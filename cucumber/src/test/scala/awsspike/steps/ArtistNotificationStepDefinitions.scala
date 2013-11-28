package awsspike.steps

import cucumber.api.scala.{EN, ScalaDsl}
import awsspike.support.Environment
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHeader
import java.io.ByteArrayInputStream
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model._
import scala.collection.JavaConversions._
import com.amazonaws.util.json.{JSONException, JSONObject}


class ArtistNotificationStepDefinitions extends ScalaDsl with EN {

  val client = Environment.httpClient
  var response: HttpResponse = null
  var messageContent:String = null

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
    val sqs = new AmazonSQSClient()
    sqs.setEndpoint("sqs.eu-west-1.amazonaws.com")
    val queueName = Environment.sqsQueueName
    val messages: List[Message] = sqs.receiveMessage(
      new ReceiveMessageRequest()
      .withQueueUrl(queueName)
      .withWaitTimeSeconds(10)
      .withMaxNumberOfMessages(5)
    ).getMessages.toList

    // there should only be one message
    for (message <- messages) {
      val json: JSONObject = new JSONObject(message.getBody)
      try {
        messageContent = json.getString("Message")
      } catch {
        case e: JSONException => {}
      }
    }
    
  }

  Then("""^the email body contains "([^"]*)"$"""){ (bodyText:String) =>
    // pick out the message on the queue that will be used in the email body
    assert(messageContent.equals("There is new content by artist Pink Floyd"))
  }

}
