package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.RoleDao;
import com.gamecity.scrabble.entity.Role;

@Repository(value = "roleDao")
class RoleDaoImpl extends AbstractDaoImpl<Role> implements RoleDao {

    @Override
    public Role getByRoleType(String type) {
        return getByNamedQuery(Constants.NamedQuery.getRoleByRoleType, Arrays.asList(Pair.of("type", type)));
    }

}
