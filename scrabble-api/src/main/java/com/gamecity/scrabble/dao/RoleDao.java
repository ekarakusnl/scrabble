package com.gamecity.scrabble.dao;

import com.gamecity.scrabble.entity.Role;
import com.gamecity.scrabble.entity.RoleType;

/**
 * Provides dao operations for {@link Role} entity
 * 
 * @author ekarakus
 */
public interface RoleDao extends BaseDao<Role> {

    /**
     * Gets the {@link Role role} by {@link RoleType type}
     * 
     * @param type <code>type</code> of the role
     * @return the role
     */
    Role getByRoleType(String type);

}
