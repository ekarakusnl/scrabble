package com.gamecity.scrabble.resource.impl;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.http.HttpStatus;

import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.UserException;

/**
 * Exception mapper for resource exceptions
 * 
 * @author ekarakus
 */
@Provider
public class ResourceExceptionMapper extends Exception implements ExceptionMapper<Throwable> {

    private static final long serialVersionUID = 7393512773904710419L;

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public Response toResponse(Throwable e) {
        if (e instanceof GameException) {
            final GameException gameException = (GameException) e;
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .entity(Mapper.toDto(gameException))
                    .build();
        } else if (e instanceof UserException) {
            final UserException userException = (UserException) e;
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .entity(Mapper.toDto(userException))
                    .build();
        }

        return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(e.getMessage()).build();
    }

}
