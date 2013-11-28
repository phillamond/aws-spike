package awsspike.support

import org.apache.http.impl.client.DefaultHttpClient
import java.util.Properties
import scala.collection.JavaConverters._


object Environment {
  val sqsQueueName = "ArtistTrackListenerQueue"
  val baseUrl = resolveBaseUrl
  init()

  private def init() {
    val environmentProperties = new Properties
    val propertySet: Set[String] = environmentProperties.stringPropertyNames.asScala.toSet
    propertySet.foreach(p => System.setProperty(p, environmentProperties.getProperty(p)))
  }

  def resolveBaseUrl: String = {
    val targetEnv = System.getProperty("env", "dev")
    if (targetEnv.equals("aws")) return "ec2url"
    "http://localhost:8080"
  }

  def httpClient: DefaultHttpClient = {
    new DefaultHttpClient
  }

}
