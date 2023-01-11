package com.gamecity.scrabble.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gamecity.scrabble.model.rest.ChatDto;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link ChatDto Chat} resources
 * 
 * @author ekarakus
 */
@CrossOrigin(allowCredentials = "true", origins = "http://web.gamecity.io")
@RestController
@RequestMapping("/rest/games/{gameId}/chats")
@Slf4j
public class ChatController extends AbstractController implements MessageListener {

    private static final String API_RESOURCE_PATH = "/games/{gameId}/chats";

    private final Map<DeferredResult<List<ChatDto>>, Pair<Long, Integer>> chats = new ConcurrentHashMap<>();

    /**
     * Sends a {@link ChatDto chat} message
     * 
     * @param gameId  <code>id</code> of the game
     * @param message chat message
     */
    @PutMapping
    @ResponseBody
    public void sendMessage(@PathVariable Long gameId, @RequestBody String message) {
        final ChatDto chatDto = new ChatDto();
        chatDto.setGameId(gameId);
        chatDto.setMessage(message);
        chatDto.setUserId(getUserId());

        put(API_RESOURCE_PATH, ChatDto.class, chatDto, gameId);
    }

    /**
     * Gets the {@link List list} of {@link ChatDto chat} by game id and action counter
     * 
     * @param gameId        <code>id</code> of the game
     * @param actionCounter acton counter
     * @return the chat list
     */
    @GetMapping
    @ResponseBody
    public DeferredResult<List<ChatDto>> getMessages(@PathVariable Long gameId, @RequestParam Integer actionCounter) {

        final DeferredResult<List<ChatDto>> deferredResult =
                new DeferredResult<>(ASYNCHRONOUS_REQUEST_DURATION, Collections.emptyList());
        chats.put(deferredResult, Pair.of(gameId, actionCounter));

        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                chats.remove(deferredResult);
            }
        });

        final List<ChatDto> newChats =
                list(API_RESOURCE_PATH + "?actionCounter={actionCounter}", ChatDto.class, gameId, actionCounter);
        if (newChats != null && newChats.size() > 0) {
            deferredResult.setResult(newChats);
        }

        return deferredResult;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            for (Entry<DeferredResult<List<ChatDto>>, Pair<Long, Integer>> entry : chats.entrySet()) {
                final List<ChatDto> newMessages = list(API_RESOURCE_PATH + "?actionCounter={actionCounter}",
                        ChatDto.class, entry.getValue().getLeft(), entry.getValue().getRight());
                if (newMessages != null) {
                    entry.getKey().setResult(newMessages);
                }
            }
        } catch (Exception e) {
            log.error("Exception : {} {}", e.getMessage(), e);
        }
    }

}
