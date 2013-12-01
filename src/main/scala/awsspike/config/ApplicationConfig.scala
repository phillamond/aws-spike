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

@Configuration
@ComponentScan(basePackages = Array("awsspike"))
@Configurable
class ApplicationConfig extends Logging {

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
    factory.setServiceBeans(controllers)
    factory.create
  }

  @Bean
  def artistTrackService: ArtistTrackService = {
    new ArtistTrackService(dynamoDBClient, snsClient)
  }

  @Bean
  def dynamoDBClient: AmazonDynamoDBClient = {
    val client: AmazonDynamoDBClient = new AmazonDynamoDBClient
    client.setEndpoint("dynamodb.eu-west-1.amazonaws.com")
    client
  }

  @Bean
  def snsClient: AmazonSNSClient = {
    val client: AmazonSNSClient = new AmazonSNSClient
    client.setEndpoint("sns.eu-west-1.amazonaws.com")
    client
  }

  @Bean
  def sqsClient: AmazonSQSClient = {
    val client: AmazonSQSClient = new AmazonSQSClient
    client.setEndpoint("sqs.eu-west-1.amazonaws.com")
    client
  }

  @Bean
  def sesClient: AmazonSimpleEmailServiceClient = {
    new AmazonSimpleEmailServiceClient
  }
}
