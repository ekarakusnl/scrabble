package com.gamecity.scrabble.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A cell represents the properties of a cell in a board
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Entity(name = "Cell")
@Table(name = "cells")
public class Cell extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = -5527872035417594800L;

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

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "has_right", nullable = false)
    private boolean hasRight;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "has_left", nullable = false)
    private boolean hasLeft;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "has_top", nullable = false)
    private boolean hasTop;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "has_bottom", nullable = false)
    private boolean hasBottom;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "center", nullable = false)
    private boolean center;

}
