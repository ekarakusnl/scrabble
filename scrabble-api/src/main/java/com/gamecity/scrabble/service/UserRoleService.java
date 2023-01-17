package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Role;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.entity.UserRole;

/**
 * Provides services for {@link Role role} assignments of the {@link User users}
 * 
 * @author ekarakus
 */
public interface UserRoleService extends BaseService<UserRole> {

    /**
     * Adds the given {@link Role role} to the {@link User user}
     * 
     * @param userId <code>id</code> of the user
     * @param role   <code>role</code> to add
     */
    void add(Long userId, Role role);

    /**
     * Gets the {@link List list} of {@link Role roles} assigned to the {@link User user}
     * 
     * @param userId <code>id</code> of the user
     * @return the list of the role types
     */
    List<Role> getRolesByUser(Long userId);

}
