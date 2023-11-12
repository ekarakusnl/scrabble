package com.gamecity.scrabble.model.rest;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents a word played in the game
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class WordDto extends AbstractDto implements Serializable {

    private static final long serialVersionUID = -5562041925029960294L;

    @JsonProperty("gameId")
    private Long gameId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("actionId")
    private Long actionId;

    @JsonProperty("roundNumber")
    private Integer roundNumber;

    @JsonProperty("word")
    private String word;

    @JsonProperty("definition")
    private String definition;

    @JsonProperty("score")
    private Integer score;

}
