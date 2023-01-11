package com.gamecity.scrabble.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents board
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class BoardDto extends AbstractDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("rowSize")
    private Integer rowSize;

    @JsonProperty("columnSize")
    private Integer columnSize;

}
