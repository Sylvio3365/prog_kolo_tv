package ws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("/hello")
public class HelloWs {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response hello() throws Exception {
        JSONObject valiny = new JSONObject();
        valiny.put("message", "Hello world ! Kolotv");
        return Response.ok(valiny.toString()).build();
    }
}
