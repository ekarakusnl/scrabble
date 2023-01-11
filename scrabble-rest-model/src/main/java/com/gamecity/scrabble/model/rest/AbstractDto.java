package com.gamecity.scrabble.model.rest;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Provides base attributes of a Dto class
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractDto {

    protected Long id;

    protected Date lastUpdatedDate;

}
