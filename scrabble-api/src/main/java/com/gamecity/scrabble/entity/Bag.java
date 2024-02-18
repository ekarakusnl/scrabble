package com.gamecity.scrabble.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A bag contains {@link Tile tiles} used in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
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

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

}
