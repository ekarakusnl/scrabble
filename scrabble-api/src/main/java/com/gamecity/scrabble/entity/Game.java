package com.gamecity.scrabble.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.gamecity.scrabble.Constants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents the properites of a game created by a {@link User user}. A game is played between
 * defined number of {@link Player players} on a {@link Board board} by using {@link Tile tiles} to
 * play {@link Word words}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Entity(name = "Game")
@Table(name = "games")
@NamedQueries({
        @NamedQuery(name = Constants.NamedQuery.searchByUser, query = "Select g from Game g, Player p "
                + " where p.gameId = g.id " + "   and p.userId = :userId " + "   and p.leftDate is null "
                + "   and g.status in ('WAITING', 'IN_PROGRESS', 'ENDED') " + " order by g.createdDate asc"),
        @NamedQuery(name = Constants.NamedQuery.searchGames, query = "Select g from Game g "
                + " where g.status = 'WAITING' " + "   and not exists (select userId from Player p "
                + "                    where p.gameId = g.id " + "                      and p.leftDate is null "
                + "                      and p.userId = :userId) " + " order by g.createdDate asc")
})
public class Game extends AbstractEntity {

    // @JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_BOARD_OWNER"))
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    // @JoinColumn(name = "rule_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_BOARD_RULE"))
    private Language language;

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

    @Column(name = "remaining_tile_count")
    private Integer remainingTileCount;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

}
