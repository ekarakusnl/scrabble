package com.gamecity.scrabble.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

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

}
