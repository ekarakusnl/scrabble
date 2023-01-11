package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Role;
import com.gamecity.scrabble.entity.RoleType;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.entity.UserRole;

/**
 * Provides services for {@link Role role} assignments to a {@link User user}
 * 
 * @author ekarakus
 */
public interface UserRoleService extends BaseService<UserRole> {

    /**
     * Adds the given {@link RoleType role type} to the {@link User user}
     * 
     * @param username <code>username</code> of the user
     * @param roleType <code>type</code> of the role
     */
    void add(String username, RoleType roleType);

    /**
     * Gets the {@link List list} of {@link RoleType role types} assigned to the {@link User user}
     * 
     * @param username <code>username</code> of the user
     * @return the list of the role types
     */
    List<RoleType> getRoleTypesByUsername(String username);

}
