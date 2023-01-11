package com.gamecity.scrabble.service;

import com.gamecity.scrabble.entity.User;

/**
 * Provides services for {@link User users} such as authorization and modification
 * 
 * @author ekarakus
 */
public interface UserService extends BaseService<User> {

    /**
     * Gets a {@link User user} by <code>username</code>
     * 
     * @param username <code>username</code> of the user
     * @return the user
     */
    User findByUsername(String username);

}
