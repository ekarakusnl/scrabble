package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.model.rest.AbstractDto;

/**
 * Rest service operations to call rest resources
 * 
 * @author ekarakus
 */
public interface RestService {

    /**
     * Calls a get resource
     * 
     * @param <T>      type of the dto
     * @param resource rest resource to call
     * @param clazz    dto class to return
     * @param params   rest resource parameters
     * @return the dto
     */
    <T extends AbstractDto> T get(String resource, Class<T> clazz, Object... params);

    /**
     * Calls a list resource
     * 
     * @param <T>      type of the dto
     * @param resource rest resource to call
     * @param clazz    dto class to return
     * @param params   rest resource parameters
     * @return the dto list
     */
    <T extends AbstractDto> List<T> list(String resource, Class<T> clazz, Object... params);

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
    <T extends AbstractDto> T post(String resource, Class<T> clazz, Object entity, Object... params);

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
    <T extends AbstractDto> T put(String resource, Class<T> clazz, T entity, Object... params);

    /**
     * Calls a delete resource
     * 
     * @param <T>
     * @param resource rest resource to call
     * @param clazz    dto class to return
     * @param params   rest resource parameters
     */
    <T extends AbstractDto> void delete(String resource, Class<T> clazz, Object... params);

}
