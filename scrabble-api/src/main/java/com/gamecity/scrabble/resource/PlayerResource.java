package com.gamecity.scrabble.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.PlayerDto;

/**
 * {@link PlayerDto Player} resources
 * 
 * @author ekarakus
 */
@Path("/games/{gameId}/players")
public interface PlayerResource {

    /**
     * Gets the {@link PlayerDto player} by <code>userId</code>
     * 
     * @param gameId <code>id</code> of the game
     * @param userId <code>id</code> of the user in the game
     * @return the player
     */
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getPlayer(@PathParam("gameId") Long gameId, @PathParam("userId") Long userId);

    /**
     * Gets the {@link List list} of {@link PlayerDto players} in a {@link GameDto game}
     * 
     * @param gameId        <code>id</code> of the game
     * @param actionCounter <code>counter</code> of the action
     * @return the list of games
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response list(@PathParam("gameId") Long gameId, @QueryParam("actionCounter") Integer actionCounter);

}
