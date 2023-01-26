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
     * Gets the {@link List list} of {@link PlayerDto players} in a {@link GameDto game}
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the action
     * @return the list of games
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response list(@PathParam("gameId") Long gameId, @QueryParam("version") Integer version);

}
