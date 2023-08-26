package com.gamecity.scrabble.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gamecity.scrabble.model.rest.UserDto;

/**
 * {@link UserDto User} resources
 * 
 * @author ekarakus
 */
@Path("/users")
public interface UserResource extends BaseResource<UserDto> {

    /**
     * Gets a {@link UserDto user} by <code>username</code>
     * 
     * @param username <code>username</code> of the user
     * @return the user
     */
    @GET
    @Path("/by/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    Response findByUsername(@PathParam("username") String username);

    /**
     * Gets a {@link UserDto user} by <code>id</code>
     * 
     * @param id <code>id</code> of the user
     * @return the user
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getUser(@PathParam("id") Long id);

}
