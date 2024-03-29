package scouter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

//Full URL Path: http://localhost:8080/ScouterAPI/rest/scouter-hello
@Path("/scouter-hello")
public class TestAPIClass {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayHello() {
		return "Hello world!";
	}
}
