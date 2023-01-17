package com.gamecity.scrabble.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamecity.scrabble.dao.ChatDao;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.service.ChatService;
import com.gamecity.scrabble.service.PlayerService;
import com.gamecity.scrabble.service.GameService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

@Service(value = "chatService")
class ChatServiceImpl extends AbstractServiceImpl<Chat, ChatDao> implements ChatService {

    private GameService gameService;
    private PlayerService playerService;
    private RedisRepository redisRepository;

    @Autowired
    void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Autowired
    void setRedisRepository(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Transactional
    @Override
    public Chat save(Chat chat) {
        final Game game = gameService.get(chat.getGameId());
        final Player player = playerService.getByUserId(game.getId(), chat.getUserId());

        if (player == null) {
            throw new GameException(GameError.NOT_IN_THE_GAME);
        }

        chat.setMessage(chat.getMessage().replace("\"", ""));

        final Chat savedChat = baseDao.save(chat);

        redisRepository.publishChat(chat.getGameId(), chat);

        return savedChat;
    }

}
