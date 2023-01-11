package com.gamecity.scrabble.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.gamecity.scrabble.Constants;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A cell represents the properties of a cell in a {@link Board board}
 * 
 * @author ekarakus
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity(name = "Cell")
@Table(name = "cells")
@NamedQuery(name = Constants.NamedQuery.getCells, query = "Select c from Cell c where c.boardId = :boardId")
public class Cell extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = -5527872035417594800L;

    // TODO fix foreign keys
    // @JoinColumn(name = "rule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_CELL_RULE"))
    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "cell_number")
    private Integer cellNumber;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "column_number")
    private Integer columnNumber;

    @Column(name = "color")
    private String color;

    @Column(name = "letter_value_multiplier")
    private Integer letterValueMultiplier;

    @Column(name = "word_score_multiplier")
    private Integer wordScoreMultiplier;

    // TODO remove definition
    @Column(name = "has_right", nullable = false, columnDefinition = "tinyint default 0")
    private boolean hasRight;

    @Column(name = "has_left", nullable = false, columnDefinition = "tinyint default 0")
    private boolean hasLeft;

    @Column(name = "has_top", nullable = false, columnDefinition = "tinyint default 0")
    private boolean hasTop;

    @Column(name = "has_bottom", nullable = false, columnDefinition = "tinyint default 0")
    private boolean hasBottom;

    @Column(name = "center", nullable = false, columnDefinition = "tinyint default 0")
    private boolean center;

}
