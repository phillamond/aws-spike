package awsspike.status

import javax.ws.rs.{Produces, GET, Path}
import javax.ws.rs.core.Response

@Path(value = "/status")
class PingAPI {

  @GET
  @Produces(Array("text/plain"))
  def ping: Response = {
    Response.ok("OK").build
  }

}