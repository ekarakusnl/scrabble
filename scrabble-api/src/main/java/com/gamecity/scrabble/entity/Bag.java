package com.gamecity.scrabble.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A bag contains {@link Tile tiles} used in a {@link Game game}
 * 
 * @author ekarakus
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity(name = "Bag")
@Table(name = "bags")
@NamedQuery(name = "loadByLanguage", query = "Select b from Bag b where b.language = :language")
public class Bag extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = -2544036251119932535L;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language;

    @Column(name = "tile_count")
    private Integer tileCount;

    @Column(name = "enabled", nullable = false, columnDefinition = "tinyint default 1")
    private boolean enabled;

}
