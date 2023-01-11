package com.gamecity.scrabble.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gamecity.scrabble.api.model.User;
import com.gamecity.scrabble.model.rest.AbstractDto;
import com.gamecity.scrabble.service.RestService;

abstract class AbstractController {

    protected Long ASYNCHRONOUS_REQUEST_DURATION = 30 * 1000L;

    @Autowired
    private RestService restService;

    /**
     * Gets authenticated user id
     * 
     * @return the user id
     */
    protected Long getUserId() {
        final AbstractAuthenticationToken auth =
                (AbstractAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (!(auth.getPrincipal() instanceof User)) {
            throw new RuntimeException("Session is not active!");
        }
        return ((User) auth.getPrincipal()).getId();
    }

    /**
     * Calls a get resource
     * 
     * @param <T>      type of the dto
     * @param resource rest resource to call
     * @param clazz    dto class to return
     * @param params   rest resource parameters
     * @return the dto
     */
    protected <T extends AbstractDto> T get(String resource, Class<T> clazz, Object... params) {
        return restService.get(resource, clazz, params);
    }

    /**
     * Calls a list resource
     * 
     * @param <T>      type of the dto
     * @param resource rest resource to call
     * @param clazz    dto class to return
     * @param params   rest resource parameters
     * @return the dto list
     */
    protected <T extends AbstractDto> List<T> list(String resource, Class<T> clazz, Object... params) {
        return restService.list(resource, clazz, params);
    }

    /**
     * Calls a post resource
     * 
     * @param <T>      type of the dto
     * @param resource rest resource to call
     * @param clazz    dto class to return
     * @param entity   entity to send
     * @param params   rest resource parameters
     * @return the dto
     */
    protected <T extends AbstractDto> T post(String resource, Class<T> clazz, Object entity, Object... params) {
        return restService.post(resource, clazz, entity, params);
    }

    /**
     * Calls a put resource
     * 
     * @param <T>      type of the dto
     * @param resource rest resource to call
     * @param clazz    dto class to return
     * @param entity   entity to send
     * @param params   rest resource parameters
     * @return the saved dto
     */
    protected <T extends AbstractDto> T put(String resource, Class<T> clazz, T entity, Object... params) {
        return restService.put(resource, clazz, entity, params);
    }

    /**
     * Calls a delete resource
     * 
     * @param <T>
     * @param resource rest resource to call
     * @param clazz    dto class to return
     * @param params   rest resource parameters
     */
    protected <T extends AbstractDto> void delete(String resource, Class<T> clazz, Object... params) {
        restService.delete(resource, clazz, params);
    }

}
