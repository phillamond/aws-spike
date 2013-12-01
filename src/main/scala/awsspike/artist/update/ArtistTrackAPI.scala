package awsspike.artist.update

import com.typesafe.scalalogging.slf4j.Logging
import play.api.libs.json._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, PutItemRequest}
import com.amazonaws.services.sns.model.PublishRequest
import scala.collection.JavaConversions.mapAsJavaMap
import javax.ws.rs.{POST, Consumes, Path}

case class Track(trackId: String, artistId: String, artistName: String)

@Path(value = "/artist/")
class ArtistTrackAPI(artistTrackService: ArtistTrackService) extends Logging {

  @Path(value = "update")
  @POST
  @Consumes(Array("application/json"))
  def addTrack(trackJson: String) = {
    val json: JsValue = Json.parse(trackJson)
    val artistId: JsValue = json \ "message" \ "params" \ "artistId"
    logger.debug("artistId: " + artistId.toString())
    val trackId: JsValue = json \ "message" \ "params" \ "tid"
    logger.debug("trackId: " + trackId.toString())
    val artistName: JsValue = json \ "message" \ "params" \ "artist" \ "artistName"
    logger.debug("artistName: " + artistName.toString())

    val track = Track(trackId.toString(), artistId.toString(), artistName.toString())
    artistTrackService.persistTrack(track)
    artistTrackService.publishTrackEvent(track)
  }
}

class ArtistTrackService(dynamoDBclient: AmazonDynamoDBClient, snsClient: AmazonSNSClient) extends Logging {
  
  def persistTrack(track: Track) = {
    val item = Map[String,AttributeValue](track.trackId -> new AttributeValue(track.artistId))
    val putItemRequest = new PutItemRequest()
      .withTableName("ArtistTracks")
      .withItem(mapAsJavaMap[String,AttributeValue](item))
    dynamoDBclient.putItem(putItemRequest)
  }

  def publishTrackEvent(track: Track) = {
    snsClient.publish(new PublishRequest)
  }

}
