package com.gamecity.scrabble.dao;

import java.util.List;

/**
 * Base dao class for CRUD operations based on entities
 * 
 * @author ekarakus
 * @param <T> Dao implementation of the type T
 */
public interface BaseDao<T> {

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
     * Deletes an entity
     * 
     * @param id <code>id</code> of the the
     */
    void delete(Long id);

    /**
     * Gets the {@link List list} of entities
     * 
     * @return the list of all entities
     */
    List<T> list();

}
