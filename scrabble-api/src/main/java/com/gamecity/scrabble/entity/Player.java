package com.gamecity.scrabble.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.gamecity.scrabble.Constants;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A player represents the {@link User user} playing in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Entity(name = "Player")
@Table(name = "players")
@NamedQueries({
        @NamedQuery(name = Constants.NamedQuery.getPlayerByUserId, query = "Select p from Player p where p.gameId = :gameId and p.userId = :userId and p.leftDate is null"),
        @NamedQuery(name = Constants.NamedQuery.getPlayerByPlayerNumber, query = "Select p from Player p where p.gameId = :gameId and p.playerNumber = :playerNumber"),
        @NamedQuery(name = Constants.NamedQuery.getCurrentPlayers, query = "Select new Player(p, u.username) from Player p, User u"
                + " where p.gameId = :gameId and p.userId = u.id and p.leftDate is null")
})
public class Player extends AbstractEntity {

    /**
     * Named query constructor
     * 
     * @param player
     * @param username
     */
    public Player(Player player, String username) {
        this.id = player.id;
        this.createdDate = player.createdDate;
        this.lastUpdatedDate = player.lastUpdatedDate;
        this.gameId = player.gameId;
        this.joinedDate = player.joinedDate;
        this.leftDate = player.leftDate;
        this.playerNumber = player.playerNumber;
        this.score = player.score;
        this.userId = player.userId;
        this.username = username;
    }

    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "user_id")
    private Long userId;

    @Transient
    private String username;

    // TODO consider a better property name
    @Column(name = "player_number")
    private Integer playerNumber;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "joined_date", nullable = false)
    private Date joinedDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "left_date")
    private Date leftDate;

}
