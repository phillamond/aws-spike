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
import com.amazonaws.auth.PropertiesCredentials


class ArtistNotificationStepDefinitions extends ScalaDsl with EN {

  val client = Environment.httpClient
  var response: HttpResponse = null
  var messageContent: String = null

  When( """^Pink Floyd release a new track$""") {
    () =>
    // no-op - just for description of scenario
  }

  When( """^a notification is POSTed to "([^"]*)" with input body$""") {
    (path: String, input: String) =>
      val httpPost = new HttpPost(Environment.baseUrl + path)
      val httpEntity = new BasicHttpEntity
      httpEntity.setContentType(new BasicHeader("Content-type", "application/json"))
      httpEntity.setContent(new ByteArrayInputStream(input.getBytes))
      httpPost.setEntity(httpEntity)
      response = client.execute(httpPost)
      assert(response.getStatusLine.getStatusCode.equals(204))
  }

  Then( """^the list of email subscribers will be sent an email$""") {
    () =>
      val credentials = new PropertiesCredentials(
        this.getClass.getResourceAsStream("/AwsCredentials.properties")
      )
      val sqs = new AmazonSQSClient(credentials)
      sqs.setEndpoint("https://sqs.eu-west-1.amazonaws.com")
      val queueName = Environment.sqsQueueName
      val messages: List[Message] = sqs.receiveMessage(
        new ReceiveMessageRequest()
          .withQueueUrl(queueName)
          .withWaitTimeSeconds(10)
          .withMaxNumberOfMessages(5)
      ).getMessages.toList

      // TODO obtain the correct message using Subject field with a UUID or something
      // since all the messages on the queue have the same message from the Pink Floyd
      // input this works anyway (including the latest message)
      // (it will fail if the last message hasn't been sent from this Cucumber test)
      for (message <- messages) {
        val json: JSONObject = new JSONObject(message.getBody)
        try {
          messageContent = json.getString("Message")
        } catch {
          case e: JSONException => {}
        }
      }

  }

  Then( """^the email body will contain "([^"]*)"$""") {
    (bodyText: String) =>
    // pick out the message on the queue that will be used in the email body
      assert(messageContent.equals("There is new content by artist Pink Floyd"))
  }

}
