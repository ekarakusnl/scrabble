package com.gamecity.scrabble.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON conversion utilities
 * 
 * @author ekarakus
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Converts a JSON payload to a dto list
     * 
     * @param <T>     type of the dto class
     * @param payload JSON payload
     * @param clazz   dto class
     * @return the dto list
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

    /**
     * Converts a JSON payload to a dto
     * 
     * @param <T>     type of the dto class
     * @param payload JSON payload
     * @param clazz   dto class
     * @return the dto object
     */
    public static <T> T toDto(String payload, Class<T> clazz) {
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
     * Converts a dto object to JSON payload
     * 
     * @param <T> type of the dto class
     * @param dto the dto to convert
     * @return the json payload
     */
    public static <T> String toJson(T dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a dto list to JSON payload
     * 
     * @param <T>     type of the dto class
     * @param dtoList the dto list to convert
     * @return the json payload
     */
    public static <T> String toJson(List<T> dtoList) {
        try {
            return objectMapper.writeValueAsString(dtoList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
