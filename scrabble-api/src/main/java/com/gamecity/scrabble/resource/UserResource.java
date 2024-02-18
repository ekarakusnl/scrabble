package com.gamecity.scrabble.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
