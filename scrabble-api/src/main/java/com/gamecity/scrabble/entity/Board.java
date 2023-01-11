package com.gamecity.scrabble.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A board represents the properties of a board used in a {@link Game game}
 * 
 * @author ekarakus
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity(name = "Board")
@Table(name = "boards")
public class Board extends AbstractEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "row_size", nullable = false)
    private Integer rowSize;

    @Column(name = "column_size", nullable = false)
    private Integer columnSize;

    @Column(name = "enabled", nullable = false, columnDefinition = "tinyint default 1")
    private boolean enabled;

}
