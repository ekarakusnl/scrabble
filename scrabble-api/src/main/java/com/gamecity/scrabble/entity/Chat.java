package com.gamecity.scrabble.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

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
        @NamedQuery(name = Constants.NamedQuery.getChatCount, query = "Select count(1) from Chat c where c.gameId = :gameId"),
        @NamedQuery(name = Constants.NamedQuery.getChats, query = "Select new Chat(c, u.username) from Chat c, User u"
                + " where c.gameId = :gameId and c.userId = u.id order by c.id asc") })
public class Chat extends AbstractEntity {

    // the default constructor
    public Chat() {
        super();
    }

    public Chat(Chat chat, String username) {
        super();
        this.id = chat.id;
        this.createdDate = chat.createdDate;
        this.lastUpdatedDate = chat.lastUpdatedDate;
        this.gameId = chat.gameId;
        this.userId = chat.userId;
        this.username = username;
        this.message = chat.message;
    }

    // @JoinColumn(name = "game_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_CHAT_GAME"))
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    // @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_CHAT_USER"))
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Transient
    private String username;

    @Column(name = "message", nullable = false)
    private String message;

}
