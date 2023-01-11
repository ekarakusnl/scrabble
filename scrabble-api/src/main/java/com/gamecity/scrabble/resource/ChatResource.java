package com.gamecity.scrabble.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.ChatDto;
import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.PlayerDto;

/**
 * {@link ChatDto Chat} resources
 * 
 * @author ekarakus
 */
@Path("/games/{gameId}/chats")
public interface ChatResource {

    /**
     * Creates a {@link ChatDto chat} by a {@link PlayerDto player }in a {@link GameDto game}
     * 
     * @param chatDto dto representation of the chat
     * @return dto representation of the chat
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response create(ChatDto chatDto);

    /**
     * Gets the {@link List list} of {@link ChatDto chats} in a {@link GameDto game}
     * 
     * @param gameId        <code>id</code> of the game
     * @param actionCounter <code>counter</code> of the action
     * @return the list of chats
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response list(@PathParam("gameId") Long gameId, @QueryParam("actionCounter") Integer actionCounter);

}
