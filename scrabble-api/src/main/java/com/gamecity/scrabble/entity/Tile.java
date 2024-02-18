package com.gamecity.scrabble.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.hibernate.annotations.Type;

import com.gamecity.scrabble.Constants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Tile represents the properties of letters in a {@link Bag bag}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Entity(name = "Tile")
@Table(name = "tiles")
@NamedQuery(name = Constants.NamedQuery.getTiles, query = "Select t from Tile t, Bag b where t.bagId = b.id and b.language = :language")
public class Tile extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 3264219155692673115L;

    // @JoinColumn(name = "rule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_TILE_RULE"))
    @Column(name = "bag_id", nullable = false)
    private Long bagId;

    @Column(name = "letter")
    private String letter;

    @Column(name = "count")
    private Integer count;

    @Column(name = "value")
    private Integer value;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "vowel", nullable = false)
    private boolean vowel;

}
