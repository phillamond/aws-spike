package awsspike.user.subscribe

import javax.ws.rs.{PUT, Consumes, Path}
import scala.Array
import javax.ws.rs.core.MediaType
import com.typesafe.scalalogging.slf4j.Logging
import play.api.libs.json._
import com.amazonaws.services.dynamodbv2.model.{PutItemRequest, AttributeValue}
import scala.collection.JavaConversions._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import javax.ws.rs.core.Response
import java.net.URI

case class UserSubscription(emailAddress: String, userName: String, artistId: String)

@Path(value = "user")
class UserEmailSubscribeAPI(userEmailSubscriptionService: UserEmailSubscriptionService) extends Logging {

  @Path(value = "/subscribe/")
  @PUT
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def addSubscription(subscriptionJson: String) = {
    val json: JsValue = Json.parse(subscriptionJson)
    val emailAddress: JsValue = json \ "userEmailAddress"
    val userName: JsValue = json \ "userName"
    val artistId: JsValue = json \ "artistId"

    val userSubscription = UserSubscription(emailAddress.as[String], userName.as[String],artistId.as[String])
    userEmailSubscriptionService.persistSubscription(userSubscription)
    Response.created(URI.create("")).build()
  }

}

class UserEmailSubscriptionService(dynamoDBclient: AmazonDynamoDBClient) extends Logging {

  def persistSubscription(userSubscription: UserSubscription) = {
    val item = Map[String,AttributeValue](
      "emailAddress" -> new AttributeValue(userSubscription.emailAddress),
      "userName" -> new AttributeValue(userSubscription.userName),
      "artistId" -> new AttributeValue(userSubscription.artistId)
    )
    val putItemRequest = new PutItemRequest()
      .withTableName(UserEmailSubscriptionService.dynamoDbTableName)
      .withItem(mapAsJavaMap[String,AttributeValue](item))
    dynamoDBclient.putItem(putItemRequest)
  }

}

object UserEmailSubscriptionService {
  val dynamoDbTableName = "ArtistTrackSubscribers"
}
