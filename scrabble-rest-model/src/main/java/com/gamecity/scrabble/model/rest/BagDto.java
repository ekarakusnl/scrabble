package com.gamecity.scrabble.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents bag
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class BagDto extends AbstractDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("language")
    private String language;

    @JsonProperty("tileCount")
    private Integer tileCount;

}
