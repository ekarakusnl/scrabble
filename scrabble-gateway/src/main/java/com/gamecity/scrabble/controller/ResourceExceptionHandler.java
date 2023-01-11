package com.gamecity.scrabble.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.gamecity.scrabble.rest.exception.GenericException;
import com.gamecity.scrabble.util.JsonUtils;

/**
 * Exception handler for resource exceptions
 * 
 * @author ekarakus
 */
@ControllerAdvice
public class ResourceExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> handleGameException(Exception e, WebRequest request) {
        if (e instanceof GenericException) {
            final GenericException genericException = (GenericException) e;
            if (genericException.getExceptionDto() != null) {
                return handleExceptionInternal(genericException, genericException.getExceptionDto(), new HttpHeaders(),
                        HttpStatus.INTERNAL_SERVER_ERROR, request);
            }

            return handleExceptionInternal(genericException, JsonUtils.toJson(genericException.getMessage()),
                    new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
        }

        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}
