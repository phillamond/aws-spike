package awsspike.event

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.simpleemail.model._
import org.springframework.scheduling.annotation.Scheduled
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import scala.collection.JavaConversions._
import play.api.libs.json.{Json, JsValue}
import com.amazonaws.services.dynamodbv2.model._
import awsspike.user.subscribe.UserEmailSubscriptionService
import com.typesafe.scalalogging.slf4j.Logging
import java.util
import com.amazonaws.services.simpleemail.model.{Destination, SendEmailRequest}


class NewTrackListener(
                        sqsClient: AmazonSQSClient,
                        dynamoDBclient: AmazonDynamoDBClient,
                        sesClient: AmazonSimpleEmailServiceClient) extends Logging {


  @Scheduled(fixedRate = 10000, initialDelay = 5000)
  def listen {
    val queueName = NewTrackListener.queueName
    val messages: List[com.amazonaws.services.sqs.model.Message] = sqsClient.receiveMessage(
      new ReceiveMessageRequest()
        .withQueueUrl(queueName)
        .withWaitTimeSeconds(10)
        .withMaxNumberOfMessages(5)
    ).getMessages.toList

    logger.debug("Listening for new tracks")
    if (messages.size > 0)
      messages.foreach(message => processMessage(message))

    def processMessage(message: com.amazonaws.services.sqs.model.Message) = {
      val json: JsValue = Json.parse(message.getBody)
      val messageContent: JsValue = json \ "Message"
      val artistId: JsValue = json \ "Subject"

      // query DynamoDb for subscribers
      val condition = new Condition()
        .withComparisonOperator(ComparisonOperator.EQ.toString())
        .withAttributeValueList(new AttributeValue().withS(artistId.as[String]))
      val keyConditions: Map[String, Condition] = Map("artistId" -> condition)

      val request = new QueryRequest()
        .withTableName(UserEmailSubscriptionService.dynamoDbTableName)
        .withKeyConditions(mapAsJavaMap(keyConditions))
        .withIndexName("artistId-index")
      val queryResult = dynamoDBclient.query(request)
      queryResult.getItems.foreach(item => sendEmailToSubscribers(item, messageContent.as[String]))
    }

    // this stuff is pure spike
    def sendEmailToSubscribers(item: util.Map[String, AttributeValue], messageContent: String) = {
      val emailAddresses: List[String] = null
      item.keySet().foreach(artistId => emailAddresses.add(artistId))
      val destination = new Destination().withToAddresses(emailAddresses)
      val subjContent = new Content().withData(NewTrackListener.emailSubject)
      val message = new com.amazonaws.services.simpleemail.model.Message().withSubject(subjContent)
      val body = new Body().withText(new Content().withData(messageContent))
      message.setBody(body)

      val request = new SendEmailRequest()
        .withSource("test@from.org")
        .withDestination(destination)
        .withMessage(message)

      sesClient.sendEmail(request)
    }
  }

}

object NewTrackListener {
  val queueName = "ArtistTrackListenerQueue"
  val emailSubject = "New artist track"
}
