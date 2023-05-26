package com.gamecity.scrabble.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.ActionDto;
import com.gamecity.scrabble.model.rest.GameDto;

/**
 * {@link ActionDto Action} resources
 * 
 * @author ekarakus
 */
@Path("/games/{gameId}/actions")
public interface ActionResource {

    /**
     * Gets the {@link ActionDto action} in the {@link GameDto game}
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the action
     * @return the action
     */
    @GET
    @Path("/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@PathParam("gameId") Long gameId, @PathParam("version") Integer version);

    /**
     * Gets the {@link List list} of {@link ActionDto actions} in a {@link GameDto game}
     * 
     * @param gameId <code>id</code> of the game
     * @return the list of actions
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response list(@PathParam("gameId") Long gameId);

}
