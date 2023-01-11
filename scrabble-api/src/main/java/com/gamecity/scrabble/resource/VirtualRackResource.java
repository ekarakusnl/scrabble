package com.gamecity.scrabble.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.model.rest.GameDto;
import com.gamecity.scrabble.model.rest.PlayerDto;
import com.gamecity.scrabble.model.rest.VirtualRackDto;
import com.gamecity.scrabble.model.rest.VirtualTileDto;

/**
 * {@link VirtualRackDto Rack} resources
 * 
 * @author ekarakus
 */
@Path("/games/{gameId}/racks")
public interface VirtualRackResource {

    /**
     * Gets the {@link VirtualRackDto rack} of the {@link PlayerDto player} in the {@link Game game}
     * 
     * @param gameId      <code>id</code> of the game
     * @param userId      <code>id</code> of the user in the game
     * @param roundNumber <code>number</code> of the round
     * @return the rack
     */
    @GET
    @Path("/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@PathParam("gameId") Long gameId, @PathParam("userId") Long userId,
            @QueryParam("roundNumber") Integer roundNumber);

    /**
     * Exchanges the {@link VirtualTileDto tile} of the {@link VirtualRackDto rack} in the {@link GameDto
     * game}
     * 
     * @param gameId     <code>id</code> of the game
     * @param userId     <code>id</code> of the user in the game
     * @param tileNumber <code>number</code> of tile to exchange
     * @return the rack
     */
    @POST
    @Path("/users/{userId}/tiles/{tileNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response exchangeTile(@PathParam("gameId") Long gameId, @PathParam("userId") Long userId,
            @PathParam("tileNumber") Integer tileNumber);

}
