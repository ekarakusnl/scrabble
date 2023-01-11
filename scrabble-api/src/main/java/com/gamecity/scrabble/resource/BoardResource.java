package com.gamecity.scrabble.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.BoardDto;

/**
 * {@link BoardDto Board} resources
 * 
 * @author ekarakus
 */
@Path("/boards")
public interface BoardResource extends BaseResource<BoardDto> {

    /**
     * Gets a {@link BoardDto board}
     * 
     * @param id <code>id</code> of the board
     * @return the board dto
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@PathParam("id") Long id);

    /**
     * Gets the {@link List list} of {@link BoardDto boards}
     * 
     * @return the list of boards
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list();

}
