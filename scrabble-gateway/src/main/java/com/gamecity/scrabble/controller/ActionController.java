package com.gamecity.scrabble.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gamecity.scrabble.model.rest.ActionDto;
import com.gamecity.scrabble.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link ActionDto Action} resources
 * 
 * @author ekarakus
 */
@RestController
@RequestMapping("/rest/games/{gameId}/actions")
@Slf4j
public class ActionController extends AbstractController implements MessageListener {

    private static final String API_RESOURCE_PATH = "/games/{gameId}/actions";

    private final Map<DeferredResult<ActionDto>, Pair<Long, Integer>> actions = new ConcurrentHashMap<>();

    /**
     * Gets the {@link List list} of {@link ActionDto actions}
     * 
     * @param gameId <code>id</code> of the game
     * @return the bag list
     */
    @GetMapping
    public ResponseEntity<List<ActionDto>> list(@PathVariable Long gameId) {
        final List<ActionDto> actions = list(API_RESOURCE_PATH, ActionDto.class, gameId);
        return new ResponseEntity<>(actions, HttpStatus.OK);
    }

    /**
     * Gets an {@link ActionDto action} by version
     * 
     * @param gameId  <code>id</code> of the game
     * @param version the expected version
     * @return the action
     */
    @GetMapping("/{version}")
    @ResponseBody
    public DeferredResult<ActionDto> getAction(@PathVariable Long gameId, @PathVariable Integer version) {

        final DeferredResult<ActionDto> deferredResult =
                new DeferredResult<>(ASYNCHRONOUS_REQUEST_DURATION, Collections.emptyList());
        actions.put(deferredResult, Pair.of(gameId, version));

        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                actions.remove(deferredResult);
            }
        });

        final ActionDto actionDto = get(API_RESOURCE_PATH + "/{version}", ActionDto.class, gameId, version);
        if (actionDto != null) {
            deferredResult.setResult(actionDto);
        }

        return deferredResult;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        final ActionDto actionDto = JsonUtils.toDto(JsonUtils.formatRedisPayload(message.toString()), ActionDto.class);

        try {
            for (Entry<DeferredResult<ActionDto>, Pair<Long, Integer>> entry : actions.entrySet()) {
                if (entry.getValue().getRight().equals(actionDto.getVersion())
                        && entry.getValue().getLeft().equals(actionDto.getGameId())) {
                    entry.getKey().setResult(actionDto);
                }
            }
        } catch (Exception e) {
            log.error("Exception : {} {}", e.getMessage(), e);
        }
    }

}
