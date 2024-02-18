package com.gamecity.scrabble.util;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * JSON conversion utilities
 * 
 * @author ekarakus
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonUtils() {
    }

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.registerModule(new JavaTimeModule());
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
            return mapper.readValue(payload, clazz);
        } catch (Exception e) {
            log.error("An error occured while converting to {} by payload {}", clazz.getSimpleName(), payload);
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
            return mapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
