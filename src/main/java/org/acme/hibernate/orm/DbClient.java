package org.acme.hibernate.orm;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/list")
@RegisterRestClient(configKey="list-api")
public interface DbClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{ten}")
    DBConnectionInfo getList(@PathParam("ten")String ten);
}
