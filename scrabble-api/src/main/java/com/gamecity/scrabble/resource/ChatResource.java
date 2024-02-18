package com.gamecity.scrabble.resource;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.ChatDto;
import com.gamecity.scrabble.model.rest.GameDto;

/**
 * {@link ChatDto Chat} resources
 * 
 * @author ekarakus
 */
@Path("/games/{gameId}/chats")
public interface ChatResource extends BaseResource<ChatDto> {

    /**
     * Gets the {@link List list} of {@link ChatDto chats} in a {@link GameDto game}
     * 
     * @param gameId <code>id</code> of the game
     * @return the list of chats
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response list(@PathParam("gameId") Long gameId);

}
