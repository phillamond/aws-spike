package awsspike.support

import org.apache.http.impl.client.DefaultHttpClient


object Environment {

  val baseUrl = "http://localhost:8080"

  def httpClient: DefaultHttpClient = {
    new DefaultHttpClient
  }

}
