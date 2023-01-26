package com.gamecity.scrabble.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
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
@RestController
@RequestMapping("/rest/games/{gameId}/chats")
@Slf4j
public class ChatController extends AbstractController implements MessageListener {

    private static final String API_RESOURCE_PATH = "/games/{gameId}/chats";

    private final Map<DeferredResult<List<ChatDto>>, Long> chats = new ConcurrentHashMap<>();

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
     * Gets the {@link List list} of {@link ChatDto chat} by game id and version
     * 
     * @param gameId       <code>id</code> of the game
     * @param messageCount <code>count</code> of the messages
     * @return the chat list
     */
    @GetMapping
    @ResponseBody
    public DeferredResult<List<ChatDto>> getMessages(@PathVariable Long gameId, @RequestParam Integer messageCount) {

        final DeferredResult<List<ChatDto>> deferredResult =
                new DeferredResult<>(ASYNCHRONOUS_REQUEST_DURATION, Collections.emptyList());
        chats.put(deferredResult, gameId);

        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                chats.remove(deferredResult);
            }
        });

        final List<ChatDto> chats = list(API_RESOURCE_PATH, ChatDto.class, gameId);
        if (chats != null && chats.size() > messageCount) {
            deferredResult.setResult(chats);
        }

        return deferredResult;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            for (Entry<DeferredResult<List<ChatDto>>, Long> entry : chats.entrySet()) {
                final List<ChatDto> messages = list(API_RESOURCE_PATH, ChatDto.class, entry.getValue());
                if (messages != null) {
                    entry.getKey().setResult(messages);
                }
            }
        } catch (Exception e) {
            log.error("Exception : {} {}", e.getMessage(), e);
        }
    }

}
