package com.gamecity.scrabble.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.rest.VirtualBoardDto;

/**
 * {@link VirtualBoardDto Board} resources
 * 
 * @author ekarakus
 */
@Path("/games/{gameId}/boards")
public interface VirtualBoardResource {

    /**
     * Gets the {@link VirtualBoard board} in the {@link Game game}
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the action
     * @return the virtual board
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@PathParam("gameId") Long gameId, @QueryParam("version") Integer version);

}
