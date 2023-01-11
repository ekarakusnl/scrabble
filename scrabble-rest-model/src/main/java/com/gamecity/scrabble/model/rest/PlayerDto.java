package com.gamecity.scrabble.model.rest;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents a player in a game
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class PlayerDto extends AbstractDto implements Serializable {

    private static final long serialVersionUID = 1429557459919580857L;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("playerNumber")
    private Integer playerNumber;

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("lastAction")
    private String lastAction;

}
