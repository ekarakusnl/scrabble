package com.gamecity.scrabble.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.gamecity.scrabble.Constants;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Word represents the word played by a {@link Player player} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "Word")
@Table(name = "words")
@NamedQuery(name = Constants.NamedQuery.getWords, query = "Select w from Word w where w.gameId = :gameId")
public class Word extends AbstractEntity {

    // @JoinColumn(name = "game_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_WORD_LOG_GAME"))
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    // @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_WORD_LOG_USER"))
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "score", nullable = false)
    private Integer score;

}
