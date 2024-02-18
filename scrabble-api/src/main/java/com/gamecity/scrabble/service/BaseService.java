package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.AbstractEntity;

/**
 * Base service class for CRUD operations based on entities
 * 
 * @author ekarakus
 * @param <T> Service implementation of the type T
 */
public interface BaseService<T extends AbstractEntity> {

    /**
     * Gets an entity by <code>id</code>
     * 
     * @param id <code>id</code> of the entity
     * @return the entity
     */
    T get(Long id);

    /**
     * Saves an entity
     * 
     * @param entity the entity to be saved
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Saves entities
     * 
     * @param entities entities to be saved
     * @return the saved entities
     */
    List<T> saveAll(List<T> entities);

}
