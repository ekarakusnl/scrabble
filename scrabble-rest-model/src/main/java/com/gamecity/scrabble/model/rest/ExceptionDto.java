package com.gamecity.scrabble.model.rest;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an exception happens during a resource call
 * 
 * @author ekarakus
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExceptionDto implements Serializable {

    private static final long serialVersionUID = 6615012161774210205L;

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("params")
    private List<String> params;

    @JsonProperty("type")
    private ExceptionType type;

}
