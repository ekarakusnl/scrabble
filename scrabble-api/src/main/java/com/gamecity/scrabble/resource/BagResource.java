package com.gamecity.scrabble.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.BagDto;

/**
 * {@link BagDto Bag} resources
 * 
 * @author ekarakus
 */
@Path("/bags")
public interface BagResource extends BaseResource<BagDto> {

    /**
     * Gets a {@link BagDto bag}
     * 
     * @param id <code>id</code> of the bag
     * @return the bag dto
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@PathParam("id") Long id);

    /**
     * Gets the {@link List list} of {@link BagDto bags}
     * 
     * @return the list of bags
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list();

}
