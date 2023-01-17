package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.UserRoleDao;
import com.gamecity.scrabble.entity.Role;
import com.gamecity.scrabble.entity.UserRole;

@Repository(value = "userRoleDao")
class UserRoleDaoImpl extends AbstractDaoImpl<UserRole> implements UserRoleDao {

    @Override
    public List<Role> getRolesByUser(Long userId) {
        return listGenericByNamedQuery(Constants.NamedQuery.getRolesByUserId, Arrays.asList(Pair.of("userId", userId)));
    }

}
