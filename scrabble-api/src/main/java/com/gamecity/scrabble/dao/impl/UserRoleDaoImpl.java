package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.UserRoleDao;
import com.gamecity.scrabble.entity.RoleType;
import com.gamecity.scrabble.entity.UserRole;

@Repository(value = "userRoleDao")
class UserRoleDaoImpl extends AbstractDaoImpl<UserRole> implements UserRoleDao {

    @Override
    public List<RoleType> getRoleTypesByUsername(String username) {
        return listGenericByNamedQuery(
                Constants.NamedQuery.getRoleTypesByUsername, Arrays.asList(Pair.of("username", username)));
    }

}
