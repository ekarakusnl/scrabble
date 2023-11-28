package com.gamecity.scrabble.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.PlayerDao;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.service.PlayerService;

@Service(value = "playerService")
class PlayerServiceImpl extends AbstractServiceImpl<Player, PlayerDao> implements PlayerService {

    @Override
    public Player add(Long gameId, Long userId, Integer playerNumber) {
        final Player player = Player.builder()
                .gameId(gameId)
                .userId(userId)
                .playerNumber(playerNumber)
                .score(0)
                .joinedDate(new Date())
                .build();
        return baseDao.save(player);
    }

    @Override
    public void remove(Player player) {
        player.setLeftDate(new Date());
        baseDao.save(player);
    }

    @Override
    public List<Player> getPlayers(Long gameId) {
        return baseDao.getCurrentPlayers(gameId);
    }

    @Override
    public Player getByUserId(Long gameId, Long userId) {
        return baseDao.getByUserId(gameId, userId);
    }

    @Override
    public Player getByPlayerNumber(Long gameId, Integer playerNumber) {
        return baseDao.getByPlayerNumber(gameId, playerNumber);
    }

    @Override
    public void updateScore(Long gameId, Integer playerNumber, Integer newWordsScore) {
        final Player player = getByPlayerNumber(gameId, playerNumber);
        player.setScore(player.getScore() + newWordsScore);
        baseDao.save(player);
    }

}
