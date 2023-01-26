package com.gamecity.scrabble.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.gamecity.scrabble.Constants;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the properites of a game created by a {@link User user}. A game is played between defined
 * number of {@link Player players} on a {@link Board board} by using {@link Tile tiles} to play
 * {@link Word words}
 * 
 * @author ekarakus
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity(name = "Game")
@Table(name = "games")
@NamedQueries({
        @NamedQuery(name = Constants.NamedQuery.getLastGames,
                query = "Select g from Game g where g.status = 'WAITING' order by g.createdDate asc"),
        @NamedQuery(name = Constants.NamedQuery.getByUser,
                query = "Select g from Game g, Player p where p.gameId = g.id and p.userId = :userId and p.leftDate is null and g.status in ('WAITING', 'IN_PROGRESS', 'LAST_ROUND') order by g.createdDate asc") })
public class Game extends AbstractEntity {

    // @JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_BOARD_OWNER"))
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "board_id", nullable = false)
    // @JoinColumn(name = "rule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_BOARD_RULE"))
    private Long boardId;

    @Column(name = "bag_id", nullable = false)
    // @JoinColumn(name = "rule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_BOARD_RULE"))
    private Long bagId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "expected_player_count", nullable = false)
    private Integer expectedPlayerCount;

    @Column(name = "active_player_count", nullable = false)
    private Integer activePlayerCount;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    // TODO add an index for status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status;

    // @JoinColumn(name = "current_player_id", referencedColumnName = "id", foreignKey =
    // @ForeignKey(name = "FK_BOARD_CURRENT_PLAYER"))
    @Column(name = "current_player_number")
    private Integer currentPlayerNumber;

    @Column(name = "round_number")
    private Integer roundNumber;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date")
    private Date endDate;

}
