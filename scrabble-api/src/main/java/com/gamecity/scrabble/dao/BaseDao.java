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
     * Gets an entity by <code>id</code> and locks it
     * 
     * @param id <code>id</code> of the entity
     * @return the entity
     */
    T getAndLock(Long id);

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

    /**
     * Gets the {@link List list} of entities
     * 
     * @return the list of all entities
     */
    List<T> list();

}
