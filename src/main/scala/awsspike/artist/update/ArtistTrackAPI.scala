package awsspike.artist.update

import com.typesafe.scalalogging.slf4j.Logging
import play.api.libs.json._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, PutItemRequest}
import com.amazonaws.services.sns.model.PublishRequest
import scala.collection.JavaConversions.mapAsJavaMap
import javax.ws.rs.{POST, Consumes, Path}
import javax.ws.rs.core.MediaType

case class Track(trackId: String, artistId: String, artistName: String)

@Path(value = "/artist/")
class ArtistTrackAPI(artistTrackService: ArtistTrackService) extends Logging {

  @Path(value = "update")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def addTrack(trackJson: String) = {
    val json: JsValue = Json.parse(trackJson)
    val artistId: JsValue = json \ "message" \ "params" \ "artistId"
    logger.debug("artistId: " + artistId.as[String])
    val trackId: JsValue = json \ "message" \ "params" \ "tid"
    logger.debug("trackId: " + trackId.as[String])
    val artistName: JsValue = json \ "message" \ "params" \ "artist" \ "artistName"
    logger.debug("artistName: " + artistName.as[String])

    val track = Track(trackId.as[String], artistId.as[String], artistName.as[String])
    artistTrackService.persistTrack(track)
    artistTrackService.publishTrackEvent(track)
  }
}

class ArtistTrackService(dynamoDBclient: AmazonDynamoDBClient, snsClient: AmazonSNSClient) extends Logging {

  def persistTrack(track: Track) = {
    val item = Map[String, AttributeValue](
      "trackId" -> new AttributeValue(track.trackId),
      "artistId" -> new AttributeValue(track.artistId),
      "artistName" -> new AttributeValue(track.artistName)
    )
    val putItemRequest = new PutItemRequest()
      .withTableName(ArtistTrackService.dynamoDbTableName)
      .withItem(mapAsJavaMap[String, AttributeValue](item))
    dynamoDBclient.putItem(putItemRequest)
  }


  def publishTrackEvent(track: Track) = {
    val message = ArtistTrackService.snsMessage(track.artistName)
    val subject = track.artistId
    logger.debug("Sending following event message to SNS topic: " + message)
    logger.debug("With message subject field: " + subject)
    val publishRequest = new PublishRequest(
      ArtistTrackService.snsTopicArn, message, subject
    )
    snsClient.publish(publishRequest)
    logger.debug("Sent message to SNS topic: " + publishRequest.getTopicArn)
  }

}

object ArtistTrackService {
  val dynamoDbTableName = "ArtistTracks"
  val snsTopicArn = "arn:aws:sns:eu-west-1:308864483436:ArtistTrackNotify"

  def snsMessage(artistName: String) = {
    s"There is new content by artist $artistName"
  }
}
