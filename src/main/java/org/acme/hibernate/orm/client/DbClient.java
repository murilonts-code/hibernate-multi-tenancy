package org.acme.hibernate.orm.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.hibernate.orm.model.DTO.DBConnectionInfo;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/list")
@RegisterRestClient(configKey="list-api")
public interface DbClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("jdbc/{ten}")
    DBConnectionInfo getList(@PathParam("ten")String ten);
}
