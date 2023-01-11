package com.gamecity.scrabble.dao;

import com.gamecity.scrabble.entity.User;

/**
 * Provides dao operations for {@link User} entity
 * 
 * @author ekarakus
 */
public interface UserDao extends BaseDao<User> {

    /**
     * Gets the {@link User user} by <code>username</code>
     * 
     * @param username <code>username</code> of the user
     * @return the user
     */
    User getByUsername(String username);

    /**
     * Gets the {@link User user} by <code>email</code>
     * 
     * @param email <code>email</code> of the user
     * @return the user
     */
    User getByEmail(String email);

}
