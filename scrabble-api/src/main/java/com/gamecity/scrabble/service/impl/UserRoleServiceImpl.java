package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.UserRoleDao;
import com.gamecity.scrabble.entity.Role;
import com.gamecity.scrabble.entity.UserRole;
import com.gamecity.scrabble.service.UserRoleService;

@Service(value = "userRoleService")
class UserRoleServiceImpl extends AbstractServiceImpl<UserRole, UserRoleDao> implements UserRoleService {

    @Override
    public void add(Long userId, Role role) {
        final UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUserId(userId);
        baseDao.save(userRole);
    }

    @Override
    public List<Role> getRolesByUser(Long userId) {
        return baseDao.getRolesByUser(userId);
    }

}
