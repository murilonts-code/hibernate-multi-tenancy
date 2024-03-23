package org.acme.hibernate.orm.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Parameters;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.hibernate.orm.model.Fruit;
import org.acme.hibernate.orm.repository.CommonRepo;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.Collection;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class FruitResource {

    private static final Logger LOGGER = Logger.getLogger(FruitResource.class.getName());

    @Inject
    CommonRepo commonRepo;

    @GET
    @Path("fruits")
    public Collection<Fruit> getDefault() {
        return get();
    }

    @GET
    @Path("{tenant}/fruits")
    public Collection<Fruit> getTenant() {
        return get();
    }

    private Collection<Fruit> get() {
        return commonRepo.find("#Fruits.findAll").list();
    }

    @GET
    @Path("fruits/{id}")
    public Fruit getSingleDefault(Long id) {
        return findById(id);
    }

    @GET
    @Path("{tenant}/fruits/{id}")
    public Fruit getSingleTenant(Long id) {
        return findById(id);
    }

    private Fruit findById(Long id) {
        return commonRepo.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Fruit with id of " + id + " does not exist.", 404));
    }

    @POST
    @Transactional
    @Path("fruits")
    public Response createDefault(Fruit fruit) {
        return create(fruit);
    }

    @POST
    @Transactional
    @Path("{tenant}/fruits")
    public Response createTenant(Fruit fruit) {
        return create(fruit);
    }

    private Response create(Fruit fruit) {
        if (fruit.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }
        LOGGER.debugv("Create {0}", fruit.getName());
        commonRepo.persist(fruit);
        return Response.ok(fruit).status(201).build();
    }

    @PUT
    @Path("fruits/{id}")
    @Transactional
    public Fruit updateDefault(Long id, Fruit fruit) {
        return update(id, fruit);
    }

    @PUT
    @Path("{tenant}/fruits/{id}")
    @Transactional
    public Fruit updateTenant(Long id, Fruit fruit) {
        return update(id, fruit);
    }

    public Fruit update(Long id, Fruit fruit) {
        if (fruit.getName() == null) {
            throw new WebApplicationException("Fruit Name was not set on request.", 422);
        }

        Fruit entity = findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.setName(fruit.getName());

        LOGGER.debugv("Update #{0} {1}", fruit.getId(), fruit.getName());

        return entity;
    }

    @DELETE
    @Path("fruits/{id}")
    @Transactional
    public Response deleteDefault(Long id) {
        return delete(id);
    }

    @DELETE
    @Path("{tenant}/fruits/{id}")
    @Transactional
    public Response deleteTenant(Long id) {
        return delete(id);
    }

    public Response delete(Long id) {
        Fruit fruit = findById(id);
        if (fruit == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        LOGGER.debugv("Delete #{0} {1}", fruit.getId(), fruit.getName());
        commonRepo.delete(fruit);
        return Response.status(204).build();
    }

    @GET
    @Path("fruitsFindBy")
    public Response findByDefault(@RestQuery String type, @RestQuery String value) {
        return findBy(type, value);
    }

    @GET
    @Path("{tenant}/fruitsFindBy")
    public Response findByTenant(@RestQuery String type, @RestQuery String value) {
        return findBy(type, value);
    }

    private Response findBy(String type, String value) {
        if (!"name".equalsIgnoreCase(type))
            throw new IllegalArgumentException("Currently only 'fruitsFindBy?type=name' is supported");

        List<Fruit> list = commonRepo.find("#Fruits.findByName", Parameters.with("name", value)).list();
        if (list.isEmpty())
            return Response.status(404).build();

        Fruit fruit = list.get(0);
        return Response.status(200).entity(fruit).build();
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {
            LOGGER.error("Failed to handle request", exception);

            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }

            ObjectNode exceptionJson = objectMapper.createObjectNode();
            exceptionJson.put("exceptionType", exception.getClass().getName());
            exceptionJson.put("code", code);

            if (exception.getMessage() != null) {
                exceptionJson.put("error", exception.getMessage());
            }

            return Response.status(code)
                    .entity(exceptionJson)
                    .build();
        }

    }
}
