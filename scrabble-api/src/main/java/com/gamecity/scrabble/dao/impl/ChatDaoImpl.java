package com.gamecity.scrabble.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;

import com.gamecity.scrabble.Constants;
import com.gamecity.scrabble.dao.ChatDao;
import com.gamecity.scrabble.entity.Chat;

@Repository(value = "chatDao")
class ChatDaoImpl extends AbstractDaoImpl<Chat> implements ChatDao {

    @Override
    public List<Chat> getChats(Long gameId) {
        return listByNamedQuery(Constants.NamedQuery.getChats, Arrays.asList(Pair.of("gameId", gameId)));
    }

}
