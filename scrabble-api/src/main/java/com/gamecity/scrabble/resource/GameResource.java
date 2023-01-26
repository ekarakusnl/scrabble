package com.gamecity.scrabble.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.ActionDto;
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
    Response get(@PathParam("id") Long id);

    /**
     * Gets the {@link List list} of {@link GameDto games}
     * 
     * @param userId <code>id</code> of the user to filter games by owner and player
     * @return the list of games
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@QueryParam("userId") @DefaultValue("") Long userId);

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
     * Gets the {@link ActionDto action} in the {@link GameDto game}
     * 
     * @param id      <code>id</code> of the game
     * @param version <code>version</code> of the action
     * @return the action
     */
    @GET
    @Path("/{id}/actions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getAction(@PathParam("id") Long id, @PathParam("version") Integer version);

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
