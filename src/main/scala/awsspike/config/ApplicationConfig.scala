package awsspike.config

import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.beans.factory.annotation.Configurable
import org.apache.cxf.endpoint.Server
import javax.ws.rs.ext.RuntimeDelegate
import javax.ws.rs.core.Application
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean
import java.util
import com.typesafe.scalalogging.slf4j.Logging
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import org.apache.cxf.bus.spring.SpringBus
import awsspike.status.PingAPI
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import awsspike.artist.update.{ArtistTrackAPI, ArtistTrackService}
import com.amazonaws.auth.PropertiesCredentials
import awsspike.user.subscribe.{UserEmailSubscriptionService, UserEmailSubscribeAPI}
import org.apache.cxf.jaxrs.provider.json.JSONProvider
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@ComponentScan(basePackages = Array("awsspike"))
@Configurable
@EnableScheduling
class ApplicationConfig extends Logging {

  val credentials = new PropertiesCredentials(
    this.getClass.getResourceAsStream("/AwsCredentials.properties")
  )

  @Bean
  def cxf: SpringBus = {
    new SpringBus
  }

  @Bean
  def jaxRsServer: Server = {
    val factory = RuntimeDelegate.getInstance().createEndpoint(new Application, classOf[JAXRSServerFactoryBean])
    val controllers = new util.ArrayList[Object]()
    controllers.add(new PingAPI)
    controllers.add(new ArtistTrackAPI(artistTrackService))
    controllers.add(new UserEmailSubscribeAPI(userEmailSubscriptionService))
    factory.setServiceBeans(controllers)
    val providers = new util.ArrayList[Object]()
    val jsonProvider = new JSONProvider
    jsonProvider.setDropRootElement(true)
    jsonProvider.setSupportUnwrapped(true)
    providers.add(jsonProvider)
    factory.setProviders(providers)
    factory.create
  }

  @Bean
  def userEmailSubscriptionService: UserEmailSubscriptionService = {
    new UserEmailSubscriptionService(dynamoDBClient)
  }

  @Bean
  def artistTrackService: ArtistTrackService = {
    new ArtistTrackService(dynamoDBClient, snsClient)
  }

  @Bean
  def dynamoDBClient: AmazonDynamoDBClient = {
    val client: AmazonDynamoDBClient = new AmazonDynamoDBClient(credentials)
    client.setEndpoint("https://dynamodb.eu-west-1.amazonaws.com")
    client
  }

  @Bean
  def snsClient: AmazonSNSClient = {
    val client: AmazonSNSClient = new AmazonSNSClient(credentials)
    client.setEndpoint("https://sns.eu-west-1.amazonaws.com")
    client
  }

  @Bean
  def sqsClient: AmazonSQSClient = {
    val client: AmazonSQSClient = new AmazonSQSClient(credentials)
    client.setEndpoint("https://sqs.eu-west-1.amazonaws.com")
    client
  }

  @Bean
  def sesClient: AmazonSimpleEmailServiceClient = {
    new AmazonSimpleEmailServiceClient(credentials)
  }
}
