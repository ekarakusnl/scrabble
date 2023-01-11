package com.gamecity.scrabble.model.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents virtual board
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class VirtualBoardDto extends AbstractDto {

    @JsonProperty("cells")
    private List<VirtualCellDto> cells;

}
