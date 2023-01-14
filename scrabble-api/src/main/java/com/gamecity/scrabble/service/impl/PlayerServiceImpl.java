package com.gamecity.scrabble.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.PlayerDao;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.UserService;

@Service(value = "playerService")
class PlayerServiceImpl extends AbstractServiceImpl<Player, PlayerDao> implements PlayerService {

    private UserService userService;

    @Autowired
    void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void add(Long gameId, Long userId, Integer playerNumber) {
        final Player player = new Player();
        player.setGameId(gameId);
        player.setUserId(userId);
        player.setPlayerNumber(playerNumber);
        player.setScore(0);
        player.setJoinedDate(new Date());

        baseDao.save(player);
    }

    @Override
    public void remove(Player player) {
        player.setLeftDate(new Date());
        baseDao.save(player);
    }

    @Override
    public List<Player> getPlayers(Long gameId) {
        final List<Player> players = baseDao.getCurrentPlayers(gameId);
        players.stream().map(this::populateUsername).collect(Collectors.toList());
        return players;
    }

    @Override
    public Player getByUserId(Long gameId, Long userId) {
        return baseDao.getByUserId(gameId, userId);
    }

    @Override
    public Player getByPlayerNumber(Long gameId, Integer playerNumber) {
        return baseDao.getByPlayerNumber(gameId, playerNumber);
    }

    // ------------------------------------ private methods ------------------------------------ //

    private Player populateUsername(Player player) {
        final User user = userService.get(player.getUserId());
        player.setUsername(user.getUsername());
        return player;
    }

}
