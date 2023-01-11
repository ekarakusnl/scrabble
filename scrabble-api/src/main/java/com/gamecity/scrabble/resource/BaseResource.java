package com.gamecity.scrabble.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.AbstractDto;

/**
 * Base resource class for resource operations based on dtos
 * 
 * @author ekarakus
 * @param <D> Resource implementation of the type D
 */
public interface BaseResource<D extends AbstractDto> {

    /**
     * Gets an entity
     * 
     * @param id <code>id</code> of the entity
     * @return dto representation of the entity
     */
    Response get(Long id);

    /**
     * Creates an entity
     * 
     * @param dto dto representation of the entity
     * @return dto representation of the entity
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response create(D dto);

    /**
     * Updates an entity
     * 
     * @param id      <code>id</code> id of the entity
     * @param dto     dto representation of the entity
     * @param ifMatch if match header
     * @param request request object
     * @return dto representation of the entity
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response update(@PathParam("id") Long id, D dto, @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context Request request);

    /**
     * Deletes an entity
     * 
     * @param id <code>id</code> of the entity
     * @return HTTP 200 if the operation is successful
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response delete(@PathParam("id") Long id);

    /**
     * Gets the {@link List list} of entities
     * 
     * @return the list of entities
     */
    Response list();

}
