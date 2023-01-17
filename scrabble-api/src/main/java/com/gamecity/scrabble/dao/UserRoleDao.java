package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.Role;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.entity.UserRole;

/**
 * Provides dao operations for {@link UserRole} entity
 * 
 * @author ekarakus
 */
public interface UserRoleDao extends BaseDao<UserRole> {

    /**
     * Gets the {@link List list} of assigned {@link Role roles} to the {@link User user} by
     * <code>userId</code>
     * 
     * @param userId <code>id</code> of the user
     * @return the role types
     */
    List<Role> getRolesByUser(Long userId);

}
