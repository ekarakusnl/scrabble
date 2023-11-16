package com.gamecity.scrabble.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.gamecity.scrabble.Constants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Word represents the word played by a {@link Player player} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "definition", nullable = false)
    private String definition;

    @Column(name = "score", nullable = false)
    private Integer score;

}
