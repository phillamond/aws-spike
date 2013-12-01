package awsspike.artist.update

import org.scalatest.{GivenWhenThen, FunSpec}
import org.scalatest.mock.MockitoSugar
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.mockito.Mockito._
import org.mockito.Matchers.any
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.dynamodbv2.model.{PutItemResult, PutItemRequest}
import com.amazonaws.services.sns.model.PublishRequest
import org.mockito.{ArgumentCaptor, BDDMockito, Matchers}

@RunWith(classOf[JUnitRunner])
class ArtistTrackAPISPec extends FunSpec with GivenWhenThen with MockitoSugar {

  describe("The artist track API") {
    val artistTrackService = mock[ArtistTrackService]

    it("accepts a post request for a new track") {
      Given("a configured instance of an ArtistTrackAPI")
      val artistTrackAPI = new ArtistTrackAPI(artistTrackService)

      When("a new track is added")
      artistTrackAPI.addTrack(readFile("/newTrack.json"))

      Then("the new track is persisted in the data store")
      verify(artistTrackService, times(1)).persistTrack(any(classOf[Track]))

      And("the artist track service publishes the event")
      verify(artistTrackService, times(1)).publishTrackEvent(any(classOf[Track]))
    }

  }

  describe("The artist track service") {
    val dynamoDBclient = mock[AmazonDynamoDBClient]
    val snsClient = mock[AmazonSNSClient]
    val track = Track("trackId", "artistId", "artistName")

    val artistTrackService = new ArtistTrackService(dynamoDBclient, snsClient)

    it("persists track and artist info in DynamoDB") {
      Given("a configured instance of an ArtistTrackService")
      val putItemResult = new PutItemResult()
      BDDMockito.given(dynamoDBclient.putItem(Matchers.any[PutItemRequest])).willReturn(putItemResult)
      val putItemCapture = ArgumentCaptor.forClass(classOf[PutItemRequest])

      When("the track and artist metadata is persisted")
      artistTrackService.persistTrack(track)

      Then("the dynamoDb client persists the data")
      verify(dynamoDBclient, times(1)).putItem(putItemCapture.capture())
    }

    it("publishes the track info on to the SNS topic") {
      Given("a configured instance of an ArtistTrackService")
      When("the track metadata is published as an SNS topic event")
      artistTrackService.publishTrackEvent(track)

      Then("the SNS client publishes the new track event")
      verify(snsClient, times(1)).publish(any(classOf[PublishRequest]))
    }
  }

  private def readFile(filepath: String): String = {
    io.Source.fromInputStream(getClass.getResourceAsStream(filepath)).mkString
  }
}
