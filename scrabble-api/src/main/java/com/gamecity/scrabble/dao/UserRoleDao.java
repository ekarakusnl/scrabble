package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.RoleType;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.entity.UserRole;

/**
 * Provides dao operations for {@link UserRole} entity
 * 
 * @author ekarakus
 */
public interface UserRoleDao extends BaseDao<UserRole> {

    /**
     * Gets the {@link List list} of assigned {@link RoleType role types} to a {@link User user} by
     * <code>username</code>
     * 
     * @param username <code>username</code> of the user
     * @return the role types
     */
    List<RoleType> getRoleTypesByUsername(String username);

}
