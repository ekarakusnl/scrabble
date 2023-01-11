package com.gamecity.scrabble.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
     * @param gameId        <code>id</code> of the game
     * @param actionCounter <code>counter</code> of the action
     * @return the virtual board
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@PathParam("gameId") Long gameId, @QueryParam("actionCounter") Integer actionCounter);

}
