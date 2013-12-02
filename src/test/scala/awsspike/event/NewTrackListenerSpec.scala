package awsspike.event

import org.scalatest.{FunSpec, GivenWhenThen}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import org.mockito.{ArgumentCaptor, Matchers, BDDMockito}
import com.amazonaws.services.sqs.model.{ReceiveMessageResult, ReceiveMessageRequest}

class NewTrackListenerSpec extends FunSpec with GivenWhenThen with MockitoSugar {

  describe("The new track listener") {
    val sqsClient = mock[AmazonSQSClient]
    val dynamoDBclient = mock[AmazonDynamoDBClient]
    val sesClient = mock[AmazonSimpleEmailServiceClient]

    it("polls SQS for new track messages and sends an email to subscribers") {
      Given("a configured instance of a new track listener")
      val newTrackListener = new NewTrackListener(sqsClient, dynamoDBclient, sesClient)
      val receiveMessageResult = new ReceiveMessageResult()
      BDDMockito.given(sqsClient.receiveMessage(Matchers.any(classOf[ReceiveMessageRequest])))
        .willReturn(receiveMessageResult)
      val receiveMessageResultCapture = ArgumentCaptor.forClass(classOf[ReceiveMessageRequest])

      When("a new track message is found")
      newTrackListener.listen

      Then("the SQS client has been invoked")
      verify(sqsClient, times(1)).receiveMessage(receiveMessageResultCapture.capture())

      And("the list of email subscribers has been discovered by the DynamoDB client")
      // TODO

      And("an email has been sent to subscribers using the SES client")
      // TODO
    }
  }

}
