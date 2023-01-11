package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.UserDao;
import com.gamecity.scrabble.entity.User;

@Repository(value = "userDao")
class UserDaoImpl extends AbstractDaoImpl<User> implements UserDao {

    @Override
    public User getByUsername(String username) {
        return getByNamedQuery(Constants.NamedQuery.getUserByUsername, Arrays.asList(Pair.of("username", username)));
    }

    @Override
    public User getByEmail(String email) {
        return getByNamedQuery(Constants.NamedQuery.getUserByEmail, Arrays.asList(Pair.of("email", email)));
    }

}
