package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.UserRoleDao;
import com.gamecity.scrabble.entity.RoleType;
import com.gamecity.scrabble.entity.UserRole;
import com.gamecity.scrabble.service.UserRoleService;

@Service(value = "userRoleService")
class UserRoleServiceImpl extends AbstractServiceImpl<UserRole, UserRoleDao> implements UserRoleService {

    @Override
    public void add(String username, RoleType type) {
        final UserRole userRole = new UserRole();
        userRole.setType(type);
        userRole.setUsername(username);
        baseDao.save(userRole);
    }

    @Override
    public List<RoleType> getRoleTypesByUsername(String username) {
        return baseDao.getRoleTypesByUsername(username);
    }

}
