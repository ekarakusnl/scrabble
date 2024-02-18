package com.gamecity.scrabble.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.gamecity.scrabble.Constants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Word represents the word played by a {@link Player player} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Entity(name = "Word")
@Table(name = "words")
@NamedQuery(name = Constants.NamedQuery.getWords, query = "Select w from Word w where w.gameId = :gameId")
public class Word extends AbstractEntity {

    // @JoinColumn(name = "game_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_WORD_GAME"))
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    // @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_WORD_USER"))
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // @JoinColumn(name = "action_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_WORD_ACTION"))
    @Column(name = "action_id", nullable = false)
    private Long actionId;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "definition")
    private String definition;

    @Column(name = "score", nullable = false)
    private Integer score;

}
