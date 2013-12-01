package awsspike.user.subscribe

import org.scalatest.{GivenWhenThen, FunSpec}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import awsspike.helper.FileHelper
import com.amazonaws.services.dynamodbv2.model.{PutItemRequest, PutItemResult}
import org.mockito.{ArgumentCaptor, Matchers, BDDMockito}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserEmailSubscribeAPISpec extends FunSpec with GivenWhenThen with MockitoSugar {

  describe("The user email subscribe API") {
  val userSubscription = UserSubscription("joe@bloggs.com", "Joe Bloggs", "test:b12a908e7e224f6892fee6a8210b7d02")

    it("accepts a PUT request for a new subscription") {
      val userEmailSubscriptionService = mock[UserEmailSubscriptionService]
      val subscriptionJson: String = FileHelper.readFile("/newSubscription.json")
      Given("a configured instance of an UserEmailSubscribeAPI")
      val userEmailSubscribeAPI = new UserEmailSubscribeAPI(userEmailSubscriptionService)

      When("a new subscription request is processed")
      userEmailSubscribeAPI.addSubscription(subscriptionJson)

      Then("the new subscription is persisted in the data store")
      verify(userEmailSubscriptionService, times(1)).persistSubscription(userSubscription)
    }

    it("persists the user subscription in DynamoDB") {
      val dynamoDBclient = mock[AmazonDynamoDBClient]
      val userEmailSubscriptionService = new UserEmailSubscriptionService(dynamoDBclient)

      Given("a configured instance of an UserEmailSubscriptionService")
      val putItemResult = new PutItemResult()
      BDDMockito.given(dynamoDBclient.putItem(Matchers.any[PutItemRequest])).willReturn(putItemResult)
      val putItemCapture = ArgumentCaptor.forClass(classOf[PutItemRequest])

      When("the user subscription metadata is persisted")
      userEmailSubscriptionService.persistSubscription(userSubscription)

      Then("the dynamoDb client persists the data")
      verify(dynamoDBclient, times(1)).putItem(putItemCapture.capture())
    }
  }
}
