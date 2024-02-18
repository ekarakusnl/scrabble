package com.gamecity.scrabble.resource;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.model.rest.WordDto;

/**
 * {@link WordDto Word} resources
 * 
 * @author ekarakus
 */
@Path("/games/{gameId}/words")
public interface WordResource {

    /**
     * Gets the {@link List list} of {@link WordDto words} in a {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @return the list of words
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@PathParam("gameId") Long gameId);

}
