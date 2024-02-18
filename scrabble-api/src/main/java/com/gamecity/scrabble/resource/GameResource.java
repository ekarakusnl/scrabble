package com.gamecity.scrabble.resource;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.VirtualRackDto;

/**
 * {@link GameDto Game} resources
 * 
 * @author ekarakus
 */
@Path("/games")
public interface GameResource extends BaseResource<GameDto> {

    /**
     * Gets the {@link GameDto game} by <code>id</code>
     * 
     * @param id <code>id</code> of the game
     * @return the game dto
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    Response get(@PathParam("id") Long id);

    /**
     * Gets the {@link List list} of {@link GameDto games}
     * 
     * @param userId      <code>id</code> of the user to filter games by owner and player
     * @param includeUser whether the games of user should be searched
     * @return the list of games
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@QueryParam("userId") @DefaultValue("") Long userId,
                           @QueryParam("includeUser") @DefaultValue("false") Boolean includeUser);

    /**
     * Joins the {@link GameDto game}
     * 
     * @param id     <code>id</code> of the game
     * @param userId <code>id</code> of the user in the game
     * @return the updated game dto
     */
    @PUT
    @Path("/{id}/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response join(@PathParam("id") Long id, @PathParam("userId") Long userId);

    /**
     * Leaves the {@link GameDto game}
     * 
     * @param id     <code>id</code> of the game
     * @param userId <code>id</code> of the user in the game
     * @return the updated game dto
     */
    @DELETE
    @Path("/{id}/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response leave(@PathParam("id") Long id, @PathParam("userId") Long userId);

    /**
     * Plays the word in the {@link GameDto game}
     * 
     * @param id      <code>id</code> of the game
     * @param userId  <code>id</code> of the user in the game
     * @param rackDto updates rack of the player
     * @return the updated game dto
     */
    @POST
    @Path("/{id}/users/{userId}/rack")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response play(@PathParam("id") Long id, @PathParam("userId") Long userId, VirtualRackDto rackDto);

}
