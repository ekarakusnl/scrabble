package com.gamecity.scrabble.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.gamecity.scrabble.Constants;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A chat represents the messages sent by the {@link Player players} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity(name = "Chat")
@Table(name = "chats")
@NamedQueries({
        @NamedQuery(name = Constants.NamedQuery.getChatCount,
                query = "Select count(1) from Chat c where c.gameId = :gameId"),
        @NamedQuery(name = Constants.NamedQuery.getChats,
                query = "Select c from Chat c where c.gameId = :gameId order by id asc") })
public class Chat extends AbstractEntity {

    // @JoinColumn(name = "game_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_CHAT_GAME"))
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    // @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_CHAT_USER"))
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "message", nullable = false)
    private String message;

}
