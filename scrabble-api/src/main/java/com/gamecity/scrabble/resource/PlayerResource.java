package com.gamecity.scrabble.resource;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
     * Gets the {@link List list} of {@link PlayerDto players} in a {@link GameDto game}version
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the action
     * @return the list of games
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response list(@PathParam("gameId") Long gameId, @QueryParam("version") Integer version);

    /**
     * Gets the {@link PlayerDto player} by <code>userId</code>
     * 
     * @param gameId  <code>id</code> of the game
     * @param userId  <code>id</code> of the user
     * @return the player
     */
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@PathParam("gameId") Long gameId, @PathParam("userId") Long userId);

}
