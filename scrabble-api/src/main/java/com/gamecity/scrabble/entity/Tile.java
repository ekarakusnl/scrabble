package com.gamecity.scrabble.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.gamecity.scrabble.Constants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Tile represents the properties of letters in a {@link Bag bag}
 * 
 * @author ekarakus
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Tile")
@Table(name = "tiles")
@NamedQuery(name = Constants.NamedQuery.getTiles, query = "Select t from Tile t where t.bagId = :bagId")
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
