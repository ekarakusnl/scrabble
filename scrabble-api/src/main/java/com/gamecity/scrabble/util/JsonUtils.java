package com.gamecity.scrabble.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON conversion utilities
 * 
 * @author ekarakus
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a JSON payload to an entity
     * 
     * @param <T>     type of the entity class
     * @param payload JSON payload
     * @param clazz   the entity class
     * @return the entity
     */
    public static <T> T toEntity(String payload, Class<T> clazz) {
        if (StringUtils.isEmpty(payload)) {
            return null;
        }

        try {
            return objectMapper.readValue(payload, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a JSON payload to an entity list
     * 
     * @param <T>     type of the entity class
     * @param payload JSON payload
     * @param clazz   the entity class
     * @return the entity list
     */

    public static <T> List<T> toList(String payload, Class<T> clazz) {
        if (StringUtils.isEmpty(payload) || clazz == null) {
            return null;
        }

        try {
            return objectMapper.readValue(payload, new TypeReference<List<T>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
