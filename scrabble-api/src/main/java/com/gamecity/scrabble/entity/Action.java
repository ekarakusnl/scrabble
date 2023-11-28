package com.gamecity.scrabble.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gamecity.scrabble.Constants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * An action represents an operation happened in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Entity(name = "Action")
@Table(name = "actions")
@NamedQueries({
        @NamedQuery(name = Constants.NamedQuery.getActions, query = "Select a from Action a where a.gameId = :gameId order by id asc"),
        @NamedQuery(name = Constants.NamedQuery.getLastAction, query = "Select a from Action a where a.gameId = :gameId and a.type != 'BONUS' order by id desc"),
        @NamedQuery(name = Constants.NamedQuery.getActionByVersion, query = "Select a from Action a where a.gameId = :gameId and a.version = :version and a.type != 'BONUS' order by id desc"),
        @NamedQuery(name = Constants.NamedQuery.getLastActionsByCount, query = "Select a from Action a where a.gameId = :gameId and a.type != 'BONUS' order by id desc")
})
public class Action extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = -8857819980114071959L;

    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "user_id")
    private Long userId;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "current_player_number")
    private Integer currentPlayerNumber;

    @Column(name = "round_number")
    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ActionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_status")
    private GameStatus gameStatus;

    @Column(name = "remaining_tile_count")
    private Integer remainingTileCount;

    @Column(name = "score")
    private Integer score;

}
