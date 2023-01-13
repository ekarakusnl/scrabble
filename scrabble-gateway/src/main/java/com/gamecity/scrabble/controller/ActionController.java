package com.gamecity.scrabble.controller;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/rest/games/{gameId}/action")
@Slf4j
public class ActionController extends AbstractController implements MessageListener {

    private static final String API_RESOURCE_PATH = "/games/{gameId}/actions";

    private final Map<DeferredResult<ActionDto>, Pair<Long, Integer>> actions = new ConcurrentHashMap<>();

    /**
     * Gets an {@link ActionDto action} by action counter
     * 
     * @param gameId  <code>id</code> of the game
     * @param counter action counter
     * @return the action
     */
    @GetMapping
    @ResponseBody
    public DeferredResult<ActionDto> getAction(@PathVariable Long gameId, @RequestParam Integer counter) {

        final DeferredResult<ActionDto> deferredResult =
                new DeferredResult<>(ASYNCHRONOUS_REQUEST_DURATION, Collections.emptyList());
        actions.put(deferredResult, Pair.of(gameId, counter));

        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                actions.remove(deferredResult);
            }
        });

        final ActionDto actionDto = get(API_RESOURCE_PATH + "/{actionCounter}", ActionDto.class, gameId, counter);
        if (actionDto != null) {
            deferredResult.setResult(actionDto);
        }

        return deferredResult;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        final ActionDto actionDto = JsonUtils.toDto(message.toString(), ActionDto.class);

        if ("READY_TO_START".equals(actionDto.getStatus())) {
            post("/games/{gameId}/start", null, null, actionDto.getGameId());
        }

        if ("READY_TO_END".equals(actionDto.getStatus())) {
            post("/games/{gameId}/end", null, null, actionDto.getGameId());
        }

        try {
            for (Entry<DeferredResult<ActionDto>, Pair<Long, Integer>> entry : actions.entrySet()) {
                log.info("onMessage is called with : {}", message.toString());

                if (entry.getValue().getRight().equals(actionDto.getCounter())
                        && entry.getValue().getLeft().equals(actionDto.getGameId())) {
                    entry.getKey().setResult(actionDto);
                }
            }
        } catch (Exception e) {
            log.error("Exception : {} {}", e.getMessage(), e);
        }
    }

}
